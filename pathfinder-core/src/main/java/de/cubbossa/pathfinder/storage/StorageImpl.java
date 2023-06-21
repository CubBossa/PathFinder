package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathapi.node.*;
import de.cubbossa.pathapi.storage.*;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonCurveLengthModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonFindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonVisualizerModifier;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
    return loadGroup(CommonPathFinder.globalGroupKey()).thenCompose(group -> {
      if (group.isPresent()) {
        return CompletableFuture.completedFuture(group.get());
      }
      return loadVisualizer(CommonPathFinder.defaultVisualizerKey()).thenApply(pathVisualizer -> {
        return pathVisualizer.orElseGet(() -> {
          return createAndLoadVisualizer(defaultVisualizerType, CommonPathFinder.defaultVisualizerKey()).join();
        });
      }).thenApply(vis -> {
        NodeGroup globalGroup = createAndLoadGroup(CommonPathFinder.globalGroupKey()).join();
        globalGroup.setWeight(0);
        globalGroup.addModifier(new CommonCurveLengthModifier(3));
        globalGroup.addModifier(new CommonFindDistanceModifier(1.5));
        globalGroup.addModifier(new CommonVisualizerModifier(vis.getKey()));
        return globalGroup;
      }).thenCompose(g -> {
        return saveGroup(g).thenApply(unused -> g);
      });
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
    Optional<NodeType<N>> cached = cache.getNodeTypeCache().getType(node);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Map<UUID, NodeType<?>> typeMapping = implementation.loadNodeTypeMapping(Set.of(node));
      Optional<NodeType<N>> type = Optional.ofNullable((NodeType<N>) typeMapping.get(node));
      type.ifPresent(t -> cache.getNodeTypeCache().write(node, t));
      return type;
    });
  }

  @Override
  public CompletableFuture<Map<UUID, NodeType<?>>> loadNodeTypes(Collection<UUID> nodes) {
    StorageCache.CacheMap<UUID, NodeType<?>> map = cache.getNodeTypeCache().getTypes(nodes);
    Map<UUID, NodeType<?>> result = new HashMap<>(map.present());

    if (map.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return asyncFuture(() -> {
      Map<UUID, NodeType<?>> newResult = implementation.loadNodeTypeMapping(map.absent());
      result.putAll(newResult);
      newResult.forEach(cache.getNodeTypeCache()::write);
      return result;
    });
  }

  // Nodes
  @Override
  public <N extends Node> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location) {
    return asyncFuture(() -> {
      N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
      implementation.saveNodeTypeMapping(Map.of(node.getNodeId(), type));
      cache.getNodeTypeCache().write(node.getNodeId(), type);

      cache.getNodeCache().write(node);
      eventDispatcher().ifPresent(e -> e.dispatchNodeCreate(node));
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(UUID id) {
    Optional<N> opt = cache.getNodeCache().getNode(id);
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt);
    }
    return this.<N>loadNodeType(id).thenApply(Optional::orElseThrow).thenApplyAsync(type -> {
      Optional<N> node = type.loadNode(id).map(this::prepareLoadedNode).map(CompletableFuture::join);
      node.ifPresent(cache.getNodeCache()::write);
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<N> insertGlobalGroupAndSave(N node) {
    if (!(node instanceof Groupable)) {
      return CompletableFuture.completedFuture(node);
    }
    return loadGroup(CommonPathFinder.globalGroupKey())
        .thenApply(Optional::orElseThrow)
        .thenCompose(group -> modifyNode(node.getNodeId(), n -> {
          ((Groupable) n).addGroup(group);
          // also modify the instance we currently work on to skip reloading the node from cache in next step
          ((Groupable) node).addGroup(group);
        }).thenApply(unused -> node));
  }

  private <N extends Node> CompletableFuture<N> insertGroups(N node) {
    if (!(node instanceof Groupable groupable)) {
      return CompletableFuture.completedFuture(node);
    }
    return loadGroups(node.getNodeId()).thenApply(g -> {
      g.forEach(groupable::addGroup);
      return node;
    });
  }

  private <N extends Node> CompletableFuture<N> insertEdges(N node) {
    return CompletableFuture.supplyAsync(() -> {
      return implementation.loadEdgesFrom(node.getNodeId());
    }).thenApply(edges -> {
      node.getEdges().addAll(edges);
      return node;
    });
  }

  private <N extends Node> CompletableFuture<N> prepareLoadedNode(N node) {
    return insertGroups(node).thenCompose(this::insertEdges);
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id) {
    Optional<N> opt = cache.getNodeCache().getNode(id);
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt);
    }
    return asyncFuture(() -> type.loadNode(id).map(this::prepareLoadedNode).map(CompletableFuture::join));
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes() {
    return cache.getNodeCache().getAllNodes()
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> {
          Collection<Node> all = nodeTypeRegistry.getTypes().stream()
              .flatMap(nodeType -> nodeType.loadAllNodes().stream())
              .map(this::prepareLoadedNode)
              .map(CompletableFuture::join)
              .collect(Collectors.toSet());
          cache.getNodeCache().writeAll(all);
          return all;
        }));
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes(Collection<UUID> ids) {
    StorageCache.CacheCollection<UUID, Node> col = cache.getNodeCache().getNodes(ids);
    Collection<Node> result = new HashSet<>(col.present());
    if (col.absent().isEmpty()) {
      return CompletableFuture.completedFuture(result);
    }
    return loadNodeTypes(col.absent()).thenApply(types -> {
      Map<NodeType<?>, Collection<UUID>> revert = new HashMap<>();
      types.forEach((uuid, nodeType) -> {
        revert.computeIfAbsent(nodeType, id -> new HashSet<>()).add(uuid);
      });
      revert.forEach((nodeType, uuids) -> {
        Collection<Node> nodes = nodeType.loadNodes(uuids).stream()
            .map(this::prepareLoadedNode)
            .map(CompletableFuture::join)
            .collect(Collectors.toSet());
        nodes.forEach(cache.getNodeCache()::write);
        result.addAll(nodes);
      });
      return result;
    });
  }

  @Override
  public CompletableFuture<Void> saveNode(Node node) {
    return saveNodeTypeSafeBlocking(node)
        .thenAccept(n -> {
          cache.getNodeCache().write(n);
          cache.getGroupCache().write(n);
        })
        .thenRun(() -> {
          eventDispatcher().ifPresent(e -> e.dispatchNodeSave(node));
        })
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  private <N extends Node> CompletableFuture<N> saveNodeTypeSafeBlocking(N node) {
    return this.<N>loadNodeType(node.getNodeId()).thenApply(Optional::orElseThrow).thenApplyAsync(type -> {
      // actually hard load and not cached to make sure that nodes are comparable
      N before = type.loadNode(node.getNodeId()).map(this::prepareLoadedNode)
          .map(CompletableFuture::join).orElseThrow();
      type.saveNode(node);

      if (node == before) {
        throw new IllegalStateException("Comparing node instance with itself while saving!");
      }

      if (before instanceof Groupable gBefore && node instanceof Groupable gAfter) {

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
      return node;
    });
  }

  @Override
  public CompletableFuture<Void> modifyNode(UUID id, Consumer<Node> updater) {
    return loadNode(id)
        .thenApply(Optional::orElseThrow)
        .thenApply(n -> {
          updater.accept(n);
          return n;
        })
        .thenCompose(this::saveNode)
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> deleteNodes(Collection<UUID> uuids) {
    return loadNodes(uuids).thenCompose(nodes -> {
      return loadNodeTypes(nodes.stream().map(Node::getNodeId).toList()).thenApplyAsync(types -> {
        for (Node node : nodes) {
          if (node instanceof Groupable groupable) {
            implementation.unassignFromGroups(groupable.getGroups(), List.of(groupable.getNodeId()));
          }
          deleteNode(node, types.get(node.getNodeId()));
          node.getEdges().forEach(implementation::deleteEdge);
        }
        implementation.deleteNodeTypeMapping(uuids);
        implementation.deleteEdgesTo(uuids);

        uuids.forEach(cache.getNodeCache()::invalidate);
        nodes.forEach(cache.getGroupCache()::invalidate);
        uuids.forEach(cache.getNodeTypeCache()::invalidate);
        return nodes;
      });
    }).thenAccept(nodes -> {
      eventDispatcher().ifPresent(e -> e.dispatchNodesDelete(nodes));
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
    return asyncFuture(() -> {
      NodeGroup group = implementation.createAndLoadGroup(key);
      cache.getGroupCache().write(group);
      return group;
    });
  }

  @Override
  public CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key) {
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
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Range range) {
    return cache.getGroupCache().getGroups(range)
        .map(CompletableFuture::completedFuture)
        .orElseGet(() -> asyncFuture(() -> implementation.loadGroups(range)));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys) {
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
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(NamespacedKey modifier) {
    Optional<Collection<NodeGroup>> cached = cache.getGroupCache().getGroups(modifier);
    return cached.map(CompletableFuture::completedFuture).orElseGet(() -> asyncFuture(() -> {
      Collection<NodeGroup> loaded = implementation.loadGroups(modifier);
      cache.getGroupCache().write(modifier, loaded);
      return loaded;
    }));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadAllGroups() {
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
    return loadGroup(group.getKey()).thenAccept(g -> {
      implementation.saveGroup(group);
      cache.getGroupCache().write(group);
      cache.getNodeCache().write(group, ComparisonResult.compare(g.orElseThrow(), group).toDelete());
    });
  }

  @Override
  public CompletableFuture<Void> modifyGroup(NamespacedKey key, Consumer<NodeGroup> update) {
    return loadGroup(key).thenApply(Optional::orElseThrow)
        .thenApply(nodeGroup -> {
          update.accept(nodeGroup);
          return nodeGroup;
        })
        .thenCompose(this::saveGroup);
  }

  @Override
  public CompletableFuture<Void> deleteGroup(NodeGroup group) {
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
      Map<NamespacedKey, VisualizerType<?>> types = implementation.loadVisualizerTypeMapping(Set.of(key));
      Optional<VisualizerType<VisualizerT>> loaded = Optional.ofNullable((VisualizerType<VisualizerT>) types.get(key));
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
      Map<NamespacedKey, VisualizerType<?>> loaded = implementation.loadVisualizerTypeMapping(map.absent());
      result.putAll(loaded);
      loaded.entrySet().forEach(cache.getVisualizerTypeCache()::write);
      return result;
    });
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Void> saveVisualizerType(
      NamespacedKey key, VisualizerType<VisualizerT> type) {
    return asyncFuture(() -> {
      implementation.saveVisualizerTypeMapping(Map.of(key, type));
      cache.getVisualizerTypeCache().write(key, type);
    });
  }

  // Visualizer

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerT> createAndLoadVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key) {
    return saveVisualizerType(key, type).thenApplyAsync(u -> {
      VisualizerT visualizer = type.getStorage().createAndLoadVisualizer(type, key);
      cache.getVisualizerCache().write(visualizer);
      return visualizer;
    });
  }

  @Override
  public CompletableFuture<Collection<PathVisualizer<?, ?>>> loadVisualizers() {
    Collection<PathVisualizer<?, ?>> result = new HashSet<>();
    return CompletableFuture.allOf(VisualizerTypeRegistryImpl.getInstance().getTypes().values().stream()
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
          Collection<VisualizerT> visualizers = type.getStorage().loadVisualizers(type).values();
          cache.getVisualizerCache().writeAll(type, visualizers);
          return visualizers;
        }));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerT>> loadVisualizer(NamespacedKey key) {
    Optional<VisualizerT> cached = cache.getVisualizerCache().getVisualizer(key);
    if (cached.isPresent()) {
      return CompletableFuture.completedFuture(cached);
    }
    return asyncFuture(() -> {
      Optional<VisualizerType<VisualizerT>> type = this.resolveOptVisualizerType(key);
      if (type.isEmpty()) {
        return Optional.empty();
      }
      Optional<VisualizerT> loaded = type.get().getStorage().loadVisualizer(type.get(), key);
      loaded.ifPresent(visualizer -> cache.getVisualizerCache().write(visualizer));
      return loaded;
    });
  }

  @Override
  public CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {

      VisualizerType<?> type = resolveVisualizerType(visualizer.getKey());
      saveVisualizerUnsafe(type, visualizer);
      cache.getVisualizerCache().write(visualizer);
    });
  }

  private void saveVisualizerUnsafe(VisualizerType type, PathVisualizer visualizer) {
    type.getStorage().saveVisualizer(type, visualizer);
  }

  @Override
  public CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {
      VisualizerType<?> type = resolveVisualizerType(visualizer.getKey());
      deleteVisualizerUnsafe(type, visualizer);
      cache.getVisualizerCache().invalidate(visualizer);
      implementation.deleteVisualizerTypeMapping(Set.of(visualizer.getKey()));
    });
  }

  private void deleteVisualizerUnsafe(VisualizerType type, PathVisualizer vis) {
    type.getStorage().deleteVisualizer(type, vis);
  }

  public <M extends Modifier> CompletableFuture<Map<Node, Collection<M>>> loadNodes(NamespacedKey modifier) {
    return loadNodes().thenApply(nodes -> {
      Map<Node, Collection<M>> results = new HashMap<>();
      nodes.stream()
          .filter(node -> node instanceof Groupable)
          .map(node -> (Groupable) node)
          .forEach(groupable -> {
            for (NodeGroup group : groupable.getGroups()) {
              group.<M>getModifier(modifier).ifPresent(m -> {
                results.computeIfAbsent(groupable, g -> new HashSet<>()).add(m);
              });
            }
          });
      return results;
    });
  }

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> resolveOptVisualizerType(
      VisualizerT visualizer) {
    return resolveOptVisualizerType(visualizer.getKey());
  }

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> resolveOptVisualizerType(NamespacedKey key) {
    return this.<VisualizerT>loadVisualizerType(key).join();
  }

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolveVisualizerType(VisualizerT visualizer) {
    return resolveVisualizerType(visualizer.getKey());
  }

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolveVisualizerType(NamespacedKey key) {
    return this.<VisualizerT>resolveOptVisualizerType(key).orElseThrow(() -> {
      return new IllegalStateException(
          "Tried to create visualizer of type '" + key + "' but could not find registered type with this key.");
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
