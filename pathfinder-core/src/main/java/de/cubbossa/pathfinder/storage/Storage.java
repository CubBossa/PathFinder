package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.api.EventDispatcher;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.Location;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.Pagination;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.api.storage.DiscoverInfo;
import de.cubbossa.pathfinder.api.storage.StorageImplementation;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.api.visualizer.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathfinder.storage.cache.EdgeCache;
import de.cubbossa.pathfinder.storage.cache.GroupCache;
import de.cubbossa.pathfinder.storage.cache.NodeCache;
import de.cubbossa.pathfinder.storage.cache.VisualizerCache;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class Storage implements de.cubbossa.pathfinder.api.storage.Storage {

  private @Nullable EventDispatcher eventDispatcher;
  private @Nullable Logger logger;

  private StorageImplementation implementation;
  private final NodeCache nodeCache = new NodeCache();
  private final EdgeCache edgeCache = new EdgeCache();
  private final GroupCache groupCache = new GroupCache();
  private final VisualizerCache visualizerCache = new VisualizerCache();
  private final DiscoverInfoCache discoverInfoCache = new DiscoverInfoCache();

  private Optional<EventDispatcher> eventDispatcher() {
    return Optional.ofNullable(eventDispatcher);
  }

  @Override
  public void init() throws Exception {
    implementation.init();
  }

  @Override
  public void shutdown() {
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
  public CompletableFuture<Void> saveNodeType(UUID node, NodeType<? extends Node<?>> type) {
    return asyncFuture(() -> implementation.saveNodeType(node, type));
  }

  @Override
  public CompletableFuture<Void> saveNodeTypes(
      Map<UUID, NodeType<? extends Node<?>>> typeMapping) {
    return asyncFuture(() -> implementation.saveNodeTypes(typeMapping));
  }

  @Override
  public <N extends Node<N>> CompletableFuture<Optional<NodeType<N>>> loadNodeType(
      UUID node) {
    return asyncFuture(() -> implementation.loadNodeType(node));
  }

  @Override
  public CompletableFuture<Map<UUID, NodeType<? extends Node<?>>>> loadNodeTypes(
      Collection<UUID> nodes) {
    return asyncFuture(() -> implementation.loadNodeTypes(nodes));
  }

  // Nodes
  @Override
  public <N extends Node<N>> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location) {
    debug("Storage: 'createAndLoadNode(" + location + ")'");
    return asyncFuture(() -> {
      N node = implementation.createAndLoadNode(type, location);
      nodeCache.write(node);
      eventDispatcher().ifPresent(e -> e.dispatchNodeCreate(node));
      return node;
    });
  }

  @Override
  public <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(UUID id) {
    debug("Storage: 'loadNode(" + id + ")'");
    return asyncFuture(() -> {
      Optional<N> opt = (Optional<N>) nodeCache.getNode(id);
      if (opt.isPresent()) {
        return opt;
      }
      Optional<N> node = implementation.loadNode(id);
      node.ifPresent(nodeCache::write);
      return node;
    });
  }

  @Override
  public <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id) {
    debug("Storage: 'loadNode(" + type.getKey() + ", " + id + ")'");
    return asyncFuture(() -> {
      Optional<N> opt = (Optional<N>) nodeCache.getNode(id);
      if (opt.isPresent()) {
        return opt;
      }
      return type.loadNode(id);
    });
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> loadNodes() {
    debug("Storage: 'loadNodes()'");
    return asyncFuture(() -> nodeCache.getAllNodes(implementation::loadNodes));
  }

  @Override
  public CompletableFuture<Collection<Node<?>>> loadNodes(Collection<UUID> ids) {
    debug("Storage: 'loadNodes(" + ids.stream().map(UUID::toString).collect(Collectors.joining(",")) + ")'");
    return asyncFuture(() -> nodeCache.getNodes(ids, implementation::loadNodes));
  }

  @Override
  public CompletableFuture<Void> saveNode(Node<?> node) {
    debug("Storage: 'saveNode(" + node.getNodeId() + ")'");
    return loadNode(node.getType(), node.getNodeId()).thenAccept(before -> {
      implementation.saveNode(node);
      nodeCache.write(node);
      groupCache.write(node);
    });
  }

  @Override
  public CompletableFuture<Void> modifyNode(UUID id, Consumer<Node<?>> updater) {
    debug("Storage: 'modifyNode(" + id + ")'");
    return loadNode(id).thenApply(n -> {
      updater.accept(n.orElseThrow());
      return n;
    }).thenCompose(n -> saveNode(n.orElseThrow()));
  }

  @Override
  public CompletableFuture<Void> deleteNodesById(Collection<UUID> uuids) {
    debug("Storage: 'deleteNodes(" + uuids.stream().map(UUID::toString).collect(Collectors.joining(",")) + ")'");
    return loadNodes(uuids).thenAccept(nodes -> {
      implementation.deleteNodes(nodes);
      uuids.forEach(nodeCache::invalidate);
      uuids.forEach(edgeCache::invalidate);
      nodes.forEach(groupCache::invalidate);
      eventDispatcher().ifPresent(e -> e.dispatchNodesDelete(uuids));
    });
  }

  @Override
  public CompletableFuture<Void> deleteNodes(Collection<Node<?>> nodes) {
    return deleteNodesById(nodes.stream().map(Node::getNodeId).toList());
  }

  // Edges
  @Override
  public CompletableFuture<Edge> createAndLoadEdge(UUID start, UUID end, double weight) {
    return asyncFuture(() -> {
      Edge edge = implementation.createAndLoadEdge(start, end, weight);
      edgeCache.write(edge);
      return edge;
    });
  }

  @Override
  public CompletableFuture<Collection<Edge>> loadEdgesFrom(UUID start) {
    return asyncFuture(() -> edgeCache.getEdgesFrom(start));
  }

  @Override
  public CompletableFuture<Collection<Edge>> loadEdgesTo(UUID end) {
    return asyncFuture(() -> edgeCache.getEdgesTo(end));
  }

  @Override
  public CompletableFuture<Optional<Edge>> loadEdge(UUID start, UUID end) {
    return asyncFuture(() -> {
      return edgeCache.getEdge(start, end, () -> implementation.loadEdge(start, end));
    });
  }

  @Override
  public CompletableFuture<Void> saveEdge(Edge edge) {
    return asyncFuture(() -> {
      implementation.saveEdge(edge);
      edgeCache.write(edge);
    });
  }

  @Override
  public CompletableFuture<Void> deleteEdge(Edge edge) {
    return asyncFuture(() -> {
      implementation.deleteEdge(edge);
      edgeCache.invalidate(edge);
    });
  }

  // Groups
  @Override
  public CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key) {
    debug("Storage: 'createAndLoadGroup(" + key + ")'");
    return asyncFuture(() -> {
      NodeGroup group = implementation.createAndLoadGroup(key);
      groupCache.write(group);
      return group;
    });
  }

  @Override
  public CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key) {
    debug("Storage: 'loadGroup(" + key + ")'");
    return asyncFuture(() -> groupCache.getGroup(key, k -> implementation.loadGroup(k).orElse(null)));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination) {
    debug("Storage: 'loadGroups(" + pagination + ")'");
    return asyncFuture(() -> groupCache.getGroups(pagination, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys) {
    debug("Storage: 'loadGroups(" + keys.stream().map(NamespacedKey::toString).collect(Collectors.joining(",")) + ")'");
    return asyncFuture(() -> groupCache.getGroups(keys, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node) {
    debug("Storage: 'loadGroups(" + node + ")'");
    return asyncFuture(() -> groupCache.getGroups(node, implementation::loadGroups));
  }

  @Override
  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier) {
    debug("Storage: 'loadGroups(" + modifier + ")'");
    return asyncFuture(() -> groupCache.getGroups(modifier, implementation::loadGroups));
  }

  @Override
  public CompletableFuture<Collection<NodeGroup>> loadAllGroups() {
    debug("Storage: 'loadAllGroups()'");
    return asyncFuture(() -> groupCache.getGroups(implementation::loadAllGroups));
  }

  @Override
  public CompletableFuture<Void> saveGroup(NodeGroup group) {
    debug("Storage: 'saveGroup(" + group.getKey() + ")'");
    return loadGroup(group.getKey()).thenAccept(g -> {
      implementation.saveGroup(group);
      groupCache.write(group);
      nodeCache.write(group, ComparisonResult.compare(g.orElseThrow(), group).toDelete());
    });
  }

  @Override
  public CompletableFuture<Void> deleteGroup(NodeGroup group) {
    debug("Storage: 'deleteGroup(" + group.getKey() + ")'");
    return asyncFuture(() -> {
      implementation.deleteGroup(group);
      groupCache.invalidate(group);
    });
  }

  // Find Data
  @Override
  public CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                                   LocalDateTime time) {
    return asyncFuture(() -> {
      DiscoverInfo info = implementation.createAndLoadDiscoverinfo(player, key, time);
      discoverInfoCache.handleNew(info);
      return info;
    });
  }
  @Override
  public CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key) {
    return asyncFuture(() -> {
      return discoverInfoCache.getDiscovery(player, key, (uuid, key1) -> implementation.loadDiscoverInfo(uuid, key1).get());
    });
  }
  @Override
  public CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info) {
    return asyncFuture(() -> {
      implementation.deleteDiscoverInfo(info);
      discoverInfoCache.handleDelete(info);
    });
  }

  // Visualizer
  @Override
  public <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
      PathVisualizer<T, ?, ?> visualizer) {
    return createAndLoadVisualizer(visualizer.getType(), visualizer.getKey());
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
      VisualizerType<T> type, NamespacedKey key) {
    return asyncFuture(() -> {
      T visualizer = type.getStorage().createAndLoadVisualizer(key);
      visualizerCache.write(visualizer);
      return visualizer;
    });
  }

  @Override
  public CompletableFuture<Collection<PathVisualizer<?, ?, ?>>> loadVisualizers() {
    return asyncFuture(() -> visualizerCache.getVisualizers(() -> {
      Collection<PathVisualizer<?, ?, ?>> visualizers = new HashSet<>();
      for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {
        visualizers.addAll(implementation.loadVisualizers(type).values());
      }
      return visualizers;
    }));
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> CompletableFuture<Map<NamespacedKey, T>> loadVisualizers(
      VisualizerType<T> type) {
    return asyncFuture(() -> visualizerCache.getVisualizers(type, t -> implementation.loadVisualizers(t).values()).stream()
        .collect(Collectors.toMap(Keyed::getKey, t -> t)));
  }

  @Override
  public <T extends PathVisualizer<T, D, ?>, D> CompletableFuture<Optional<T>> loadVisualizer(
      NamespacedKey key) {
    return asyncFuture(() -> visualizerCache.getVisualizer(key, k -> {
      for (VisualizerType<? extends PathVisualizer<?,?, ?>> type : VisualizerHandler.getInstance().getVisualizerTypes()) {
        Optional<PathVisualizer<?, ?, ?>> opt = (Optional<PathVisualizer<?, ?, ?>>) type.getStorage().loadVisualizer(key);
        if (opt.isPresent()) return (T) opt.get();
      }
      return null;
    }));
  }

  @Override
  public CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.saveVisualizer(visualizer);
      visualizerCache.write(visualizer);
    });
  }

  @Override
  public CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.deleteVisualizer(visualizer);
      visualizerCache.invalidate(visualizer);
    });
  }

  private void debug(String message) {
    if (logger != null) logger.log(Level.INFO, message);
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
      if (!toDelete.isEmpty()) consumer.accept(toDelete);
    }

    public void toInsertIfPresent(Consumer<Collection<T>> consumer) {
      if (!toInsert.isEmpty()) consumer.accept(toInsert);
    }
  }
}
