package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

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
  public <N extends Node> CompletableFuture<Optional<NodeType<N>>> loadNodeType(
      UUID node) {
    return asyncFuture(() -> implementation.loadNodeType(node));
  }

  @Override
  public CompletableFuture<Map<UUID, NodeType<? extends Node>>> loadNodeTypes(
      Collection<UUID> nodes) {
    return asyncFuture(() -> implementation.loadNodeTypes(nodes));
  }

  // Nodes
  @Override
  public <N extends Node> CompletableFuture<N> createAndLoadNode(NodeType<N> type,
                                                                 Location location) {
    debug("Storage: 'createAndLoadNode(" + location + ")'");
    return asyncFuture(() -> {
      N node = implementation.createAndLoadNode(type, location);
      cache.getNodeCache().write(node);
      eventDispatcher().ifPresent(e -> e.dispatchNodeCreate(node));
      return node;
    });
  }

  @Override
  public <N extends Node> CompletableFuture<Optional<N>> loadNode(UUID id) {
    debug("Storage: 'loadNode(" + id + ")'");
    return asyncFuture(() -> {
      Optional<N> opt = (Optional<N>) cache.getNodeCache().getNode(id);
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
      Optional<N> opt = (Optional<N>) cache.getNodeCache().getNode(id);
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
    });
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
    return null;
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
    return null;
  }

  // Visualizer
  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerT> createAndLoadVisualizer(
      VisualizerT visualizer) {
    return createAndLoadVisualizer(cache.getVisualizerTypeCache()
        .getType(visualizer.getKey(), implementation::loadVisualizerType), visualizer.getKey());
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerT> createAndLoadVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key) {
    return asyncFuture(() -> {
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
    return asyncFuture(() -> cache.getVisualizerCache().getVisualizer(key, k -> {
      for (VisualizerType<? extends PathVisualizer<?, ?>> type : VisualizerHandler.getInstance()
          .getTypes()) {
        Optional<PathVisualizer<?, ?>> opt =
            (Optional<PathVisualizer<?, ?>>) type.getStorage().loadVisualizer(key);
        if (opt.isPresent()) {
          return (VisualizerT) opt.get();
        }
      }
      return null;
    }));
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
