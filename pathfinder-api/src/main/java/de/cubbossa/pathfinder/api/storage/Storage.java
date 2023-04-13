package de.cubbossa.pathfinder.api.storage;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.Pagination;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Storage {
  void init() throws Exception;

  void shutdown();

  CompletableFuture<Void> saveNodeType(UUID node,
                                       de.cubbossa.pathfinder.api.node.NodeType<? extends Node<?>> type);

  CompletableFuture<Void> saveNodeTypes(Map<UUID, NodeType<? extends Node<?>>> typeMapping);

  <N extends Node<N>> CompletableFuture<Optional<NodeType<N>>> loadNodeType(UUID node);

  CompletableFuture<Map<UUID, NodeType<? extends Node<?>>>> loadNodeTypes(Collection<UUID> nodes);

  // Nodes
  <N extends Node<N>> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location);

  CompletableFuture<Void> modifyNode(UUID id, Consumer<Node<?>> updater);

  <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(UUID id);

  <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(
      NodeType<N> type, UUID id);

  CompletableFuture<Collection<Node<?>>> loadNodes();

  CompletableFuture<Collection<Node<?>>> loadNodes(Collection<UUID> ids);

  CompletableFuture<Void> saveNode(Node<?> node);

  CompletableFuture<Void> deleteNodesById(Collection<UUID> uuids);

  CompletableFuture<Void> deleteNodes(Collection<Node<?>> nodes);

  // Edges
  CompletableFuture<Edge> createAndLoadEdge(UUID start, UUID end, double weight);

  CompletableFuture<Collection<Edge>> loadEdgesFrom(UUID start);

  CompletableFuture<Collection<Edge>> loadEdgesTo(UUID end);

  CompletableFuture<Optional<Edge>> loadEdge(UUID start, UUID end);

  CompletableFuture<Void> saveEdge(Edge edge);

  CompletableFuture<Void> deleteEdge(Edge edge);

  // Waypoint
  CompletableFuture<Waypoint> createAndLoadWaypoint(Location location);

  CompletableFuture<Optional<Waypoint>> loadWaypoint(UUID uuid);

  CompletableFuture<Collection<Waypoint>> loadAllWaypoints();

  CompletableFuture<Collection<Waypoint>> loadWaypoints(Collection<UUID> uuids);

  CompletableFuture<Void> saveWaypoint(Waypoint waypoint);

  CompletableFuture<Void> deleteWaypoints(Collection<Waypoint> waypoints);

  // Groups
  CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key);

  CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys);

  CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node);

  <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier);

  CompletableFuture<Collection<NodeGroup>> loadAllGroups();

  CompletableFuture<Collection<Node<?>>> loadGroupNodes(NodeGroup group);

  CompletableFuture<Void> saveGroup(NodeGroup group);

  CompletableFuture<Void> deleteGroup(NodeGroup group);

  // Find Data
  CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                            LocalDateTime time);

  CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key);

  CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer
  <T extends PathVisualizer<T, ?>> CompletableFuture<T> createAndLoadVisualizer(
      PathVisualizer<T, ?> visualizer);

  <T extends PathVisualizer<T, ?>> CompletableFuture<T> createAndLoadVisualizer(
      VisualizerType<T> type, NamespacedKey key);

  CompletableFuture<Collection<PathVisualizer<?, ?>>> loadVisualizers();

  <T extends PathVisualizer<T, ?>> CompletableFuture<Map<NamespacedKey, T>> loadVisualizers(
      VisualizerType<T> type);

  <T extends PathVisualizer<T, D>, D> CompletableFuture<Optional<T>> loadVisualizer(
      NamespacedKey key);

  CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?> visualizer);

  CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?> visualizer);

  de.cubbossa.pathfinder.storage.cache.NodeCache getNodeCache();

  de.cubbossa.pathfinder.storage.cache.EdgeCache getEdgeCache();

  de.cubbossa.pathfinder.storage.cache.GroupCache getGroupCache();

  de.cubbossa.pathfinder.storage.cache.VisualizerCache getVisualizerCache();

  de.cubbossa.pathfinder.storage.cache.DiscoverInfoCache getDiscoverInfoCache();
}
