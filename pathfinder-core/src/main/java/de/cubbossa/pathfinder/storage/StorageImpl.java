package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.storage.StorageImplementation;
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
  private @Nullable EventDispatcher eventDispatcher;
  private @Nullable Logger logger;
  private StorageImplementation implementation;

  public StorageImpl() {
    cache = new CacheLayerImpl();
  }

  private Optional<EventDispatcher> eventDispatcher() {
    return Optional.ofNullable(eventDispatcher);
  }

  @Override
  public void init() throws Exception {
    implementation.setCache(cache);
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
  public <N extends Node> CompletableFuture<NodeType<N>> loadNodeType(
      UUID node) {
    return asyncFuture(() -> cache.getNodeTypeCache().getType(node, implementation::loadNodeType));
  }

  @Override
  public CompletableFuture<Map<UUID, NodeType<?>>> loadNodeTypes(
      Collection<UUID> nodes) {
    return asyncFuture(() -> cache.getNodeTypeCache().getTypes(nodes, implementation::loadNodeTypes));
  }

  // Nodes
  @Override
  public <N extends Node> CompletableFuture<N> createAndLoadNode(NodeType<N> type,
                                                                 Location location) {
    debug("Storage: 'createAndLoadNode(" + location + ")'");
    return asyncFuture(() -> {
      N node = implementation.createAndLoadNode(type, location);
      cache.getNodeCache().write(node);
      cache.getNodeTypeCache().write(node.getNodeId(), type);
      eventDispatcher().ifPresent(e -> e.dispatchNodeCreate(node));
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(UUID id) {
    debug("Storage: 'loadNode(" + id + ")'");
    return asyncFuture(() -> {
      Optional<N> opt = cache.getNodeCache().getNode(id);
      if (opt.isPresent()) {
        return opt;
      }
      Optional<N> node = implementation.loadNode(id);
      node.ifPresent(cache.getNodeCache()::write);
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id) {
    debug("Storage: 'loadNode(" + type.getKey() + ", " + id + ")'");
    return asyncFuture(() -> {
      Optional<N> opt = cache.getNodeCache().getNode(id);
      if (opt.isPresent()) {
        return opt;
      }
      return type.loadNode(id);
    });
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes() {
    debug("Storage: 'loadNodes()'");
    return asyncFuture(() -> cache.getNodeCache().getAllNodes(implementation::loadNodes));
  }

  @Override
  public CompletableFuture<Collection<Node>> loadNodes(Collection<UUID> ids) {
    debug("Storage: 'loadNodes(" + ids.stream().map(UUID::toString).collect(Collectors.joining(","))
        + ")'");
    return asyncFuture(() -> cache.getNodeCache().getNodes(ids, implementation::loadNodes));
  }

  @Override
  public CompletableFuture<Void> saveNode(Node node) {
    debug("Storage: 'saveNode(" + node.getNodeId() + ")'");
    NodeType<?> type =
        cache.getNodeTypeCache().getType(node.getNodeId(), implementation::loadNodeType);
    return loadNode(type, node.getNodeId()).thenAccept(before -> {
      eventDispatcher().ifPresent(e -> e.dispatchNodeSave(node));
      implementation.saveNode(node);
      cache.getNodeCache().write(node);
      cache.getGroupCache().write(node);
    });
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

    return asyncFuture(() -> {
      eventDispatcher().ifPresent(e -> e.dispatchNodesDelete(nodes));
      implementation.deleteNodes(nodes);
      uuids.forEach(cache.getNodeCache()::invalidate);
      nodes.forEach(cache.getGroupCache()::invalidate);
      uuids.forEach(cache.getNodeTypeCache()::invalidate);
    });
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
    return asyncFuture(
        () -> cache.getGroupCache().getGroup(key, k -> implementation.loadGroup(k).orElse(null)));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination) {
    debug("Storage: 'loadGroups(" + pagination + ")'");
    return asyncFuture(
        () -> cache.getGroupCache().getGroups(pagination, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys) {
    debug("Storage: 'loadGroups(" + keys.stream().map(NamespacedKey::toString)
        .collect(Collectors.joining(",")) + ")'");
    return asyncFuture(() -> cache.getGroupCache().getGroups(keys, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node) {
    debug("Storage: 'loadGroups(" + node + ")'");
    return asyncFuture(() -> cache.getGroupCache().getGroups(node, implementation::loadGroups));
  }

  @Override
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(
      Class<M> modifier) {
    debug("Storage: 'loadGroups(" + modifier + ")'");
    return asyncFuture(() -> cache.getGroupCache().getGroups(modifier, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadAllGroups() {
    debug("Storage: 'loadAllGroups()'");
    return asyncFuture(() -> cache.getGroupCache().getGroups(implementation::loadAllGroups));
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
  public CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player,
                                                                    NamespacedKey key) {
    return asyncFuture(() -> {
      return cache.getDiscoverInfoCache().getDiscovery(player, key,
          (uuid, key1) -> implementation.loadDiscoverInfo(uuid, key1).get());
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
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerType<VisualizerT>> loadVisualizerType(
      NamespacedKey key) {
    return asyncFuture(() -> {
      return (VisualizerType<VisualizerT>) cache.getVisualizerTypeCache().getType(key, implementation::loadVisualizerType).orElseThrow(() -> {
        return new IllegalStateException("Tried to create visualizer of type '" + key + "' but could not find registered type with this key.");
      });
    });
  }

  @Override
  public CompletableFuture<Map<NamespacedKey, VisualizerType<?>>> loadVisualizerTypes(
      Collection<NamespacedKey> keys) {
    return asyncFuture(() -> {
      return cache.getVisualizerTypeCache().getTypes(keys, implementation::loadVisualizerTypes);
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
    return asyncFuture(() -> cache.getVisualizerCache().getVisualizers(() -> {
      Collection<PathVisualizer<?, ?>> visualizers = new HashSet<>();
      for (VisualizerType<?> type : VisualizerHandler.getInstance().getTypes()) {
        visualizers.addAll(implementation.loadVisualizers(type).values());
      }
      return visualizers;
    }));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Map<NamespacedKey, VisualizerT>> loadVisualizers(
      VisualizerType<VisualizerT> type) {
    return asyncFuture(
        () -> cache.getVisualizerCache()
            .getVisualizers(type, t -> implementation.loadVisualizers(t).values())
            .stream()
            .collect(Collectors.toMap(Keyed::getKey, t -> t)));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerT>> loadVisualizer(
      NamespacedKey key) {
    return asyncFuture(() -> cache.getVisualizerCache().getVisualizer(key, k -> implementation.loadVisualizer(k)));
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
