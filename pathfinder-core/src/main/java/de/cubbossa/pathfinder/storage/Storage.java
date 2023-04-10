package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathfinder.storage.cache.EdgeCache;
import de.cubbossa.pathfinder.storage.cache.GroupCache;
import de.cubbossa.pathfinder.storage.cache.NodeCache;
import de.cubbossa.pathfinder.storage.cache.VisualizerCache;
import de.cubbossa.pathfinder.util.Pagination;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

@Getter
@RequiredArgsConstructor
public class Storage {

  private final PathFinder pathFinder;
  private final StorageImplementation implementation;
  private final NodeCache nodeCache = new NodeCache();
  private final EdgeCache edgeCache = new EdgeCache();
  private final GroupCache groupCache = new GroupCache();
  private final VisualizerCache visualizerCache = new VisualizerCache();
  private final DiscoverInfoCache discoverInfoCache = new DiscoverInfoCache();

  public void init() throws Exception {
    implementation.init();
  }

  public void shutdown() {
    implementation.shutdown();
  }

  private CompletableFuture<Void> asyncFuture(Runnable runnable) {
    return CompletableFuture.runAsync(runnable);
  }

  private <T> CompletableFuture<T> asyncFuture(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier);
  }

  // Node Type

  public CompletableFuture<Void> saveNodeType(UUID node, NodeType<?> type) {
    return asyncFuture(() -> implementation.saveNodeType(node, type));
  }

  public CompletableFuture<Void> saveNodeTypes(Map<UUID, NodeType<?>> typeMapping) {
    return asyncFuture(() -> implementation.saveNodeTypes(typeMapping));
  }

  public <N extends Node<N>> CompletableFuture<Optional<NodeType<N>>> loadNodeType(UUID node) {
    return asyncFuture(() -> implementation.loadNodeType(node));
  }

  public CompletableFuture<Map<UUID, NodeType<?>>> loadNodeTypes(Collection<UUID> nodes) {
    return asyncFuture(() -> implementation.loadNodeTypes(nodes));
  }

  // Nodes
  public <N extends Node<N>> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location) {
    return asyncFuture(() -> {
      N node = implementation.createAndLoadNode(type, location);
      nodeCache.write(node);
      pathFinder.getEventDispatcher().dispatchNodeCreate(node);
      return node;
    });
  }

  public CompletableFuture<Void> modifyNode(UUID id, Consumer<Node<?>> updater) {
    return loadNode(id).thenApply(n -> {
      updater.accept(n.orElseThrow());
      return n;
    }).thenCompose(n -> saveNode(n.orElseThrow()));
  }

  public <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(UUID id) {
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

  public <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id) {
    return asyncFuture(() -> {
      Optional<N> opt = (Optional<N>) nodeCache.getNode(id);
      if (opt.isPresent()) {
        return opt;
      }
      return type.loadNode(id);
    });
  }

  public CompletableFuture<Collection<Node<?>>> loadNodes() {
    return asyncFuture(() -> nodeCache.getAllNodes(implementation::loadNodes));
  }

  public CompletableFuture<Collection<Node<?>>> loadNodes(Collection<UUID> ids) {
    return asyncFuture(() -> nodeCache.getNodes(ids, implementation::loadNodes));
  }

  public CompletableFuture<Void> saveNode(Node<?> node) {
    return asyncFuture(() -> {
      implementation.saveNode(node);
      nodeCache.write(node);
    });
  }

  public CompletableFuture<Void> deleteNodesById(Collection<UUID> uuids) {
    return loadNodes(uuids).thenAccept(nodes -> {
      implementation.deleteNodes(nodes);
      uuids.forEach(nodeCache::invalidate);
      uuids.forEach(edgeCache::invalidate);
      pathFinder.getEventDispatcher().dispatchNodesDelete(uuids);
    });
  }

  public CompletableFuture<Void> deleteNodes(Collection<Node<?>> nodes) {
    return deleteNodesById(nodes.stream().map(Node::getNodeId).toList());
  }

  // Edges
  public CompletableFuture<Edge> createAndLoadEdge(UUID start, UUID end, double weight) {
    return asyncFuture(() -> {
      Edge edge = implementation.createAndLoadEdge(start, end, weight);
      edgeCache.write(edge);
      return edge;
    });
  }

  public CompletableFuture<Collection<Edge>> loadEdgesFrom(UUID start) {
    return asyncFuture(() -> edgeCache.getEdgesFrom(start));
  }

  public CompletableFuture<Collection<Edge>> loadEdgesTo(UUID end) {
    return asyncFuture(() -> edgeCache.getEdgesTo(end));
  }

  public CompletableFuture<Optional<Edge>> loadEdge(UUID start, UUID end) {
    return asyncFuture(() -> {
      return edgeCache.getEdge(start, end, () -> implementation.loadEdge(start, end));
    });
  }

  public CompletableFuture<Void> saveEdge(Edge edge) {
    return asyncFuture(() -> {
      implementation.saveEdge(edge);
      edgeCache.write(edge);
    });
  }

  public CompletableFuture<Void> deleteEdge(Edge edge) {
    return asyncFuture(() -> {
      implementation.deleteEdge(edge);
      edgeCache.invalidate(edge);
    });
  }

  // Waypoint
  public CompletableFuture<Waypoint> createAndLoadWaypoint(Location location) {
    return asyncFuture(() -> {
      Waypoint waypoint = implementation.createAndLoadWaypoint(location);
      nodeCache.write(waypoint);
      return waypoint;
    });
  }

  public CompletableFuture<Optional<Waypoint>> loadWaypoint(UUID uuid) {
    return asyncFuture(() -> {
      Optional<Node<?>> opt = nodeCache.getNode(uuid);
      if (opt.isEmpty()) {
        return implementation.loadNode(uuid);
      }
      return opt.get() instanceof Waypoint waypoint ? Optional.of(waypoint) : Optional.empty();
    });
  }

  public CompletableFuture<Collection<Waypoint>> loadAllWaypoints() {
    return asyncFuture(() -> {
      Collection<Waypoint> waypoints = implementation.loadAllWaypoints();
      waypoints.forEach(nodeCache::write);
      return waypoints;
    });
  }

  public CompletableFuture<Collection<Waypoint>> loadWaypoints(Collection<UUID> uuids) {
    return asyncFuture(() -> {
      Collection<Node<?>> present = nodeCache.getNodes(uuids, implementation::loadWaypoints);
      return present.stream()
          .filter(node -> node instanceof Waypoint)
          .map(node -> (Waypoint) node)
          .toList();
    });
  }

  public CompletableFuture<Void> saveWaypoint(Waypoint waypoint) {
    return asyncFuture(() -> {
      implementation.saveWaypoint(waypoint);
      nodeCache.write(waypoint);
    });
  }

  public CompletableFuture<Void> deleteWaypoints(Collection<Waypoint> waypoints) {
    return asyncFuture(() -> {
      implementation.deleteWaypoints(waypoints);
      waypoints.stream().map(Node::getNodeId).forEach(nodeCache::invalidate);
    });
  }

  // Groups
  public CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key) {
    return asyncFuture(() -> {
      NodeGroup group = implementation.createAndLoadGroup(key);
      groupCache.write(group);
      return group;
    });
  }

  public CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key) {
    return asyncFuture(() -> groupCache.getGroup(key, k -> implementation.loadGroup(k).orElseThrow()));
  }

  public CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination) {
    return asyncFuture(() -> groupCache.getGroups(pagination, implementation::loadGroups));
  }

  public CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys) {
    return asyncFuture(() -> groupCache.getGroups(keys, implementation::loadGroups));
  }

  public CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node) {
    return asyncFuture(() -> groupCache.getGroups(node, implementation::loadGroups));
  }

  public <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier) {
    return asyncFuture(() -> groupCache.getGroups(modifier, implementation::loadGroups));
  }

  public CompletableFuture<Collection<NodeGroup>> loadAllGroups() {
    return asyncFuture(() -> groupCache.getGroups(implementation::loadAllGroups));
  }

  public CompletableFuture<Collection<Node<?>>> loadGroupNodes(NodeGroup group) {
    return null;
  }

  public CompletableFuture<Void> saveGroup(NodeGroup group) {
    return asyncFuture(() -> {
      implementation.saveGroup(group);
      groupCache.write(group);
    });
  }

  public CompletableFuture<Void> deleteGroup(NodeGroup group) {
    return asyncFuture(() -> {
      implementation.deleteGroup(group);
      groupCache.invalidate(group);
    });
  }

  // Find Data
  public CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time) {
    return asyncFuture(() -> {
      DiscoverInfo info = implementation.createAndLoadDiscoverinfo(player, key, time);
      discoverInfoCache.handleNew(info);
      return info;
    });
  }
  public CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key) {
    return asyncFuture(() -> {
      return discoverInfoCache.getDiscovery(player, key, (uuid, key1) -> implementation.loadDiscoverInfo(uuid, key1).get());
    });
  }
  public CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info) {
    return asyncFuture(() -> {
      implementation.deleteDiscoverInfo(info);
      discoverInfoCache.handleDelete(info);
    });
  }

  // Visualizer
  public <T extends PathVisualizer<T, ?>> CompletableFuture<T> createAndLoadVisualizer(PathVisualizer<T, ?> visualizer) {
    return createAndLoadVisualizer(visualizer.getType(), visualizer.getKey());
  }

  public <T extends PathVisualizer<T, ?>> CompletableFuture<T> createAndLoadVisualizer(VisualizerType<T> type, NamespacedKey key) {
    return asyncFuture(() -> {
      T visualizer = type.getStorage().createAndLoadVisualizer(key);
      visualizerCache.write(visualizer);
      return visualizer;
    });
  }

  public CompletableFuture<Collection<PathVisualizer<?, ?>>> loadVisualizers() {
    return asyncFuture(() -> visualizerCache.getVisualizers(() -> {
      Collection<PathVisualizer<?, ?>> visualizers = new HashSet<>();
      for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {
        visualizers.addAll(implementation.loadVisualizers(type).values());
      }
      return visualizers;
    }));
  }

  public <T extends PathVisualizer<T, ?>> CompletableFuture<Map<NamespacedKey, T>> loadVisualizers(VisualizerType<T> type) {
    return asyncFuture(() -> visualizerCache.getVisualizers(type, t -> implementation.loadVisualizers(t).values()).stream()
        .collect(Collectors.toMap(Keyed::getKey, t -> t)));
  }

  public <T extends PathVisualizer<T, D>, D> CompletableFuture<Optional<T>> loadVisualizer(NamespacedKey key) {
    return asyncFuture(() -> visualizerCache.getVisualizer(key, k -> {
      for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {
        Optional<PathVisualizer<?, ?>> opt = (Optional<PathVisualizer<?, ?>>) type.getStorage().loadVisualizer(key);
        if (opt.isPresent()) return (T) opt.get();
      }
      return null;
    }));
  }

  public CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.saveVisualizer(visualizer);
      visualizerCache.write(visualizer);
    });
  }

  public CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?> visualizer) {
    return asyncFuture(() -> {
      implementation.deleteVisualizer(visualizer);
      visualizerCache.invalidate(visualizer);
    });
  }
}
