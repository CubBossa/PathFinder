package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.*;
import de.cubbossa.pathapi.storage.*;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.CurveLengthModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
public class StorageImpl implements Storage {

  private CacheLayer cache;
  private @Nullable EventDispatcher<?> eventDispatcher;
  private @Nullable Logger logger;
  private StorageImplementation implementation;

  private final NodeTypeRegistry nodeTypeRegistry;

  public StorageImpl(NodeTypeRegistry nodeTypeRegistry) {
    cache = new CacheLayerImpl();
    this.nodeTypeRegistry = nodeTypeRegistry;
  }

  private Optional<EventDispatcher<?>> eventDispatcher() {
    return Optional.ofNullable(eventDispatcher);
  }

  @Override
  public void init() throws Exception {
    implementation.init();
  }

  @Override
  public void shutdown() {
    for (StorageCache<?> cache : this.cache) {
      cache.invalidateAll();
    }
    implementation.shutdown();
  }

  @Override
  public CompletableFuture<NodeGroup> createGlobalNodeGroup(VisualizerType<?> defaultVisualizerType) {
    return loadGroup(CommonPathFinder.globalGroupKey()).thenApply(group -> {
      if (group.isPresent()) {
        return group.get();
      }
      PathVisualizer<?, ?> vis = loadVisualizer(CommonPathFinder.defaultVisualizerKey()).thenApply(pathVisualizer -> {
        return pathVisualizer.orElseGet(() -> {
          return createAndLoadVisualizer(defaultVisualizerType, CommonPathFinder.defaultVisualizerKey()).join();
        });
      }).join();

      NodeGroup globalGroup = createAndLoadGroup(CommonPathFinder.globalGroupKey()).join();
      globalGroup.setWeight(0);
      globalGroup.addModifier(new CurveLengthModifier(3));
      globalGroup.addModifier(new FindDistanceModifier(1.5));
      globalGroup.addModifier(new VisualizerModifier(vis));
      saveGroup(globalGroup).join();
      return globalGroup;
    });
  }

  private CompletableFuture<Void> asyncFuture(Runnable runnable) {
    return CompletableFuture.runAsync(runnable).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  private <T> CompletableFuture<T> asyncFuture(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  // Node Type

  @Override
  public <N extends Node> CompletableFuture<Optional<NodeType<N>>> loadNodeType(UUID node) {
    debug("Storage: 'loadNodeType(" + node + ")'");
    Optional<NodeType<N>> cached = cache.getNodeTypeCache().getType(node);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Optional<NodeType<N>> type = implementation.loadNodeType(node);
      type.ifPresent(t -> cache.getNodeTypeCache().write(node, t));
      return type;
    });
  }

  @Override
  public CompletableFuture<Map<UUID, NodeType<?>>> loadNodeTypes(Collection<UUID> nodes) {
    debug("Storage: 'loadNodeTypes(" + nodes + ")'");
    StorageCache.CacheMap<UUID, NodeType<?>> map = cache.getNodeTypeCache().getTypes(nodes);
    Map<UUID, NodeType<?>> result = new HashMap<>(map.present());

    if (map.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return asyncFuture(() -> {
      Map<UUID, NodeType<?>> newResult = implementation.loadNodeTypes(map.absent());
      result.putAll(newResult);
      newResult.forEach(cache.getNodeTypeCache()::write);
      return result;
    });
  }

  // Nodes
  @Override
  public <N extends Node> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location) {
    debug("Storage: 'createAndLoadNode(" + location + ")'");
    return asyncFuture(() -> {
      N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
      if (node instanceof Groupable groupable) {
        loadGroup(CommonPathFinder.globalGroupKey()).join().ifPresent(groupable::addGroup);
      }
      implementation.saveNodeType(node.getNodeId(), type);
      cache.getNodeTypeCache().write(node.getNodeId(), type);

      cache.getNodeCache().write(node);
      eventDispatcher().ifPresent(e -> e.dispatchNodeCreate(node));
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(UUID id) {
    debug("Storage: 'loadNode(" + id + ")'");
    Optional<N> opt = cache.getNodeCache().getNode(id);
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt);
    }
    return asyncFuture(() -> {
      NodeType<N> type = this.<N>loadNodeType(id).join().orElseThrow();
      Optional<N> node = type.loadNode(id).map(this::prepareLoadedNode);
      node.ifPresent(cache.getNodeCache()::write);
      return node;
    });
  }

  private <N extends Node> N insertGroups(N node) {
    if (!(node instanceof Groupable groupable)) {
      return node;
    }
    CompletableFuture.allOf(
        loadGroup(CommonPathFinder.globalGroupKey()).thenAccept(o -> o.ifPresent(groupable::addGroup)),
        loadGroups(node.getNodeId()).thenAccept(g -> g.forEach(groupable::addGroup))
    ).join();
    return node;
  }

  private <N extends Node> N insertEdges(N node) {
    node.getEdges().addAll(implementation.loadEdgesFrom(node.getNodeId()));
    return node;
  }

  private <N extends Node> Optional<N> prepareLoadedNode(Optional<N> node) {
    return node.map(this::prepareLoadedNode);
  }

  private <N extends Node> N prepareLoadedNode(N node) {
    return insertGroups(insertEdges(node));
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id) {
    debug("Storage: 'loadNode(" + type.getKey() + ", " + id + ")'");
    Optional<N> opt = cache.getNodeCache().getNode(id);
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt);
    }
    return asyncFuture(() -> prepareLoadedNode(type.loadNode(id)));
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes() {
    debug("Storage: 'loadNodes()'");
    return cache.getNodeCache().getAllNodes()
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> {
          Collection<Node> all = nodeTypeRegistry.getTypes().stream()
              .flatMap(nodeType -> nodeType.loadAllNodes().stream())
              .map(this::prepareLoadedNode)
              .collect(Collectors.toSet());
          cache.getNodeCache().writeAll(all);
          return all;
        }));
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes(Collection<UUID> ids) {
    debug("Storage: 'loadNodes(" + ids.stream().map(UUID::toString).collect(Collectors.joining(",")) + ")'");
    StorageCache.CacheCollection<UUID, Node> col = cache.getNodeCache().getNodes(ids);
    Collection<Node> result = new HashSet<>(col.present());
    if (col.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return asyncFuture(() -> {
      Map<UUID, NodeType<?>> types = loadNodeTypes(col.absent()).join();
      Map<NodeType<?>, Collection<UUID>> revert = new HashMap<>();
      types.forEach((uuid, nodeType) -> {
        revert.computeIfAbsent(nodeType, id -> new HashSet<>()).add(uuid);
      });
      revert.forEach((nodeType, uuids) -> {
        Collection<Node> nodes = nodeType.loadNodes(uuids).stream()
            .map(this::prepareLoadedNode)
            .collect(Collectors.toSet());
        nodes.forEach(cache.getNodeCache()::write);
        result.addAll(nodes);
      });
      return result;
    });
  }

  @Override
  public CompletableFuture<Void> saveNode(Node node) {
    debug("Storage: 'saveNode(" + node.getNodeId() + ")'");
    return loadNodeType(node.getNodeId())
        .thenCompose(type -> loadNode(type.orElseThrow(), node.getNodeId()))
        .thenApply(before -> {
          eventDispatcher().ifPresent(e -> e.dispatchNodeSave(node));
          cache.getNodeCache().write(node);
          cache.getGroupCache().write(node);
          return before;
        })
        .thenAcceptAsync(before -> saveNodeTypeSafeBlocking(node));
  }

  private <N extends Node> void saveNodeTypeSafeBlocking(N node) {
    NodeType<N> type = this.<N>loadNodeType(node.getNodeId()).join().orElseThrow();
    // actually hard load and not cached to make sure that nodes are comparable
    N before = type.loadNode(node.getNodeId()).map(this::prepareLoadedNode).orElseThrow();
    type.saveNode(node);

    if (node == before) {
      throw new IllegalStateException("Comparing node instance with itself while saving!");
    }

    if (before instanceof Groupable gBefore && node instanceof Groupable gAfter) {
      loadGroup(CommonPathFinder.globalGroupKey()).join().ifPresent(gAfter::addGroup);

      StorageImpl.ComparisonResult<NodeGroup> cmp =
          StorageImpl.ComparisonResult.compare(gBefore.getGroups(), gAfter.getGroups());
      cmp.toInsertIfPresent(nodeGroups -> implementation.assignToGroups(nodeGroups, List.of(node.getNodeId())));
      cmp.toDeleteIfPresent(nodeGroups -> implementation.unassignFromGroups(nodeGroups, List.of(node.getNodeId())));
    }
    StorageImpl.ComparisonResult<Edge> cmp = StorageImpl.ComparisonResult.compare(before.getEdges(), node.getEdges());
    cmp.toInsertIfPresent(edges -> {
      for (Edge edge : edges) {
        implementation.createAndLoadEdge(edge.getStart(), edge.getEnd(), edge.getWeight());
      }
    });
    cmp.toDeleteIfPresent(edges -> edges.forEach(implementation::deleteEdge));
  }

  @Override
  public CompletableFuture<Void> modifyNode(UUID id, Consumer<Node> updater) {
    debug("Storage: 'modifyNode(" + id + ")'");
    return loadNode(id).thenApply(n -> {
      updater.accept(n.orElseThrow());
      return n;
    }).thenCompose(n -> saveNode(n.orElseThrow()));
  }

  @Override
  public CompletableFuture<Void> deleteNodesById(Collection<UUID> uuids) {
    return loadNodes(uuids).thenAccept(this::deleteNodes);
  }

  @Override
  public CompletableFuture<Void> deleteNodes(Collection<Node> nodes) {
    Collection<UUID> uuids = nodes.stream().map(Node::getNodeId).toList();
    debug("Storage: 'deleteNodes(" + uuids.stream().map(UUID::toString)
        .collect(Collectors.joining(",")) + ")'");

    eventDispatcher().ifPresent(e -> e.dispatchNodesDelete(nodes));
    return asyncFuture(() -> {

      Map<UUID, NodeType<?>> types = loadNodeTypes(nodes.stream().map(Node::getNodeId).toList()).join();
      for (Node node : nodes) {
        if (node instanceof Groupable groupable) {
          implementation.unassignFromGroups(groupable.getGroups(), List.of(groupable.getNodeId()));
        }
        deleteNode(node, types.get(node.getNodeId()));
        node.getEdges().forEach(implementation::deleteEdge);
        // TODO delete edges to
      }
      // TODO remove Type mapping, remove edge mapping

      uuids.forEach(cache.getNodeCache()::invalidate);
      nodes.forEach(cache.getGroupCache()::invalidate);
      uuids.forEach(cache.getNodeTypeCache()::invalidate);
    });
  }

  private void deleteNode(Node node, NodeType type) {
    type.deleteNode(node);
  }

  @Override
  public CompletableFuture<Collection<Edge>> loadEdgesTo(Collection<Node> nodes) {
    return asyncFuture(() -> nodes.stream().parallel()
        .map(Node::getNodeId)
        .map(node -> implementation.loadEdgesTo(node))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet()));
  }

  // Groups
  @Override
  public CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key) {
    debug("Storage: 'createAndLoadGroup(" + key + ")'");
    return asyncFuture(() -> {
      NodeGroup group = implementation.createAndLoadGroup(key);
      cache.getGroupCache().write(group);
      return group;
    });
  }

  @Override
  public CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key) {
    debug("Storage: 'loadGroup(" + key + ")'");
    Optional<NodeGroup> group = cache.getGroupCache().getGroup(key);
    if (group.isPresent()) {
      return CompletableFuture.completedFuture(group);
    }
    return asyncFuture(() -> {
      Optional<NodeGroup> loaded = implementation.loadGroup(key);
      loaded.ifPresent(g -> cache.getGroupCache().write(g));
      return loaded;
    });
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination) {
    debug("Storage: 'loadGroups(" + pagination + ")'");
    return cache.getGroupCache().getGroups(pagination)
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> implementation.loadGroups(pagination)));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys) {
    debug("Storage: 'loadGroups(" + keys.stream().map(NamespacedKey::toString).collect(Collectors.joining(",")) + ")'");
    StorageCache.CacheCollection<NamespacedKey, NodeGroup> cached = cache.getGroupCache().getGroups(keys);
    Collection<NodeGroup> result = new HashSet<>(cached.present());
    if (cached.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return asyncFuture(() -> {
      Collection<NodeGroup> loaded = implementation.loadGroups(cached.absent());
      loaded.forEach(cache.getGroupCache()::write);
      result.addAll(loaded);
      return result;
    });
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node) {
    debug("Storage: 'loadGroups(" + node + ")'");
    Optional<Collection<NodeGroup>> cached = cache.getGroupCache().getGroups(node);
    return cached
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> {
          Collection<NodeGroup> loaded = implementation.loadGroups(node);
          cache.getGroupCache().write(node, loaded);
          return loaded;
        }));
  }

  @Override
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier) {
    debug("Storage: 'loadGroups(" + modifier + ")'");
    Optional<Collection<NodeGroup>> cached = cache.getGroupCache().getGroups(modifier);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached.get());
    }
    return asyncFuture(() -> {
      Collection<NodeGroup> loaded = implementation.loadGroups(modifier);
      cache.getGroupCache().write(modifier, loaded);
      return loaded;
    });
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadAllGroups() {
    debug("Storage: 'loadAllGroups()'");
    Optional<Collection<NodeGroup>> cached = cache.getGroupCache().getGroups();
    return cached
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> {
          Collection<NodeGroup> groups = implementation.loadAllGroups();
          cache.getGroupCache().writeAll(groups);
          return groups;
        }));
  }

  @Override
  public CompletableFuture<Void> saveGroup(NodeGroup group) {
    debug("Storage: 'saveGroup(" + group.getKey() + ")'");
    return loadGroup(group.getKey()).thenAccept(g -> {
      implementation.saveGroup(group);
      cache.getGroupCache().write(group);
      cache.getNodeCache()
          .write(group, ComparisonResult.compare(g.orElseThrow(), group).toDelete());
    });
  }

  @Override
  public CompletableFuture<Void> modifyGroup(NamespacedKey key, Consumer<NodeGroup> update) {
    return loadGroup(key).thenApply(Optional::orElseThrow).thenAccept(group -> {
      update.accept(group);
      saveGroup(group).join();
    });
  }

  @Override
  public CompletableFuture<Void> deleteGroup(NodeGroup group) {
    debug("Storage: 'deleteGroup(" + group.getKey() + ")'");
    return asyncFuture(() -> {
      implementation.deleteGroup(group);
      cache.getGroupCache().invalidate(group);
    });
  }

  // Find Data
  @Override
  public CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                                   LocalDateTime time) {
    return asyncFuture(() -> {
      DiscoverInfo info = implementation.createAndLoadDiscoverinfo(player, key, time);
      cache.getDiscoverInfoCache().write(info);
      return info;
    });
  }

  @Override
  public CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key) {
    Optional<DiscoverInfo> cached = cache.getDiscoverInfoCache().getDiscovery(player, key);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Optional<DiscoverInfo> info = implementation.loadDiscoverInfo(player, key);
      info.ifPresent(i -> cache.getDiscoverInfoCache().write(i));
      return info;
    });
  }

  @Override
  public CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info) {
    return asyncFuture(() -> {
      implementation.deleteDiscoverInfo(info);
      cache.getDiscoverInfoCache().invalidate(info);
    });
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerType<VisualizerT>>> loadVisualizerType(
      NamespacedKey key) {
    Optional<VisualizerType<VisualizerT>> cached = cache.getVisualizerTypeCache().getType(key);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Optional<VisualizerType<VisualizerT>> loaded = implementation.loadVisualizerType(key);
      loaded.ifPresent(type -> cache.getVisualizerTypeCache().write(key, type));
      return loaded;
    });
  }

  @Override
  public CompletableFuture<Map<NamespacedKey, VisualizerType<?>>> loadVisualizerTypes(Collection<NamespacedKey> keys) {
    StorageCache.CacheMap<NamespacedKey, VisualizerType<?>> map = cache.getVisualizerTypeCache().getTypes(keys);
    HashMap<NamespacedKey, VisualizerType<?>> result = new HashMap<>(map.present());
    if (map.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return asyncFuture(() -> {
      Map<NamespacedKey, VisualizerType<?>> loaded = implementation.loadVisualizerTypes(map.absent());
      result.putAll(loaded);
      loaded.entrySet().forEach(cache.getVisualizerTypeCache()::write);
      return result;
    });
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Void> saveVisualizerType(
      NamespacedKey key, VisualizerType<VisualizerT> type) {
    return asyncFuture(() -> {
      implementation.saveVisualizerType(key, type);
      cache.getVisualizerTypeCache().write(key, type);
    });
  }

  // Visualizer

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerT> createAndLoadVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key) {
    return asyncFuture(() -> {
      saveVisualizerType(key, type).join();
      VisualizerT visualizer = type.getStorage().createAndLoadVisualizer(key);
      cache.getVisualizerCache().write(visualizer);
      return visualizer;
    });
  }

  @Override
  public CompletableFuture<Collection<PathVisualizer<?, ?>>> loadVisualizers() {
    Collection<PathVisualizer<?, ?>> result = new HashSet<>();
    return CompletableFuture.allOf(VisualizerHandler.getInstance().getTypes().values().stream()
            .map(this::loadVisualizers)
            .map(c -> c.thenApply(result::addAll))
            .toArray(CompletableFuture[]::new))
        .thenApply(unused -> result);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Collection<VisualizerT>> loadVisualizers(
      VisualizerType<VisualizerT> type) {

    Optional<Collection<VisualizerT>> cached = cache.getVisualizerCache().getVisualizers(type);
    return cached
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> {
          Map<NamespacedKey, VisualizerT> loaded = implementation.loadVisualizers(type);
          cache.getVisualizerCache().writeAll(type, loaded.values());
          return loaded.values();
        }));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerT>> loadVisualizer(NamespacedKey key) {
    Optional<VisualizerT> cached = cache.getVisualizerCache().getVisualizer(key);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Optional<VisualizerT> loaded = implementation.loadVisualizer(key);
      loaded.ifPresent(visualizer -> cache.getVisualizerCache().write(visualizer));
      return loaded;
    });
  }

  @Override
  public CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.saveVisualizer(visualizer);
      cache.getVisualizerCache().write(visualizer);
    });
  }

  @Override
  public CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.deleteVisualizer(visualizer);
      cache.getVisualizerCache().invalidate(visualizer);
    });
  }

  private void debug(String message) {
    if (logger != null) {
      logger.log(Level.INFO, message);
    }
  }

  public <M extends Modifier> CompletableFuture<Map<Node, Collection<M>>> loadNodes(Class<M> modifier) {
    return loadNodes().thenApply(nodes -> {
      Map<Node, Collection<M>> results = new HashMap<>();
      nodes.stream()
          .filter(node -> node instanceof Groupable)
          .map(node -> (Groupable) node)
          .forEach(groupable -> {
            for (NodeGroup group : groupable.getGroups()) {
              if (group.hasModifier(modifier)) {
                results.computeIfAbsent(groupable, g -> new HashSet<>()).add(group.getModifier(modifier));
              }
            }
          });
      return results;
    });
  }

  public record ComparisonResult<T>(Collection<T> toDelete, Collection<T> toInsert) {

    public static <T> ComparisonResult<T> compare(Collection<T> before, Collection<T> after) {
      return compare(before, after, HashSet::new);
    }

    public static <T> ComparisonResult<T> compare(Collection<T> before, Collection<T> after,
                                                  Function<Collection<T>, Collection<T>> collector) {
      Collection<T> toDelete = collector.apply(before);
      toDelete.removeAll(after);
      Collection<T> toInsert = collector.apply(after);
      toInsert.removeAll(before);
      return new ComparisonResult<>(toDelete, toInsert);
    }

    public void toDeleteIfPresent(Consumer<Collection<T>> consumer) {
      if (!toDelete.isEmpty()) {
        consumer.accept(toDelete);
      }
    }

    public void toInsertIfPresent(Consumer<Collection<T>> consumer) {
      if (!toInsert.isEmpty()) {
        consumer.accept(toInsert);
      }
    }
  }
}
