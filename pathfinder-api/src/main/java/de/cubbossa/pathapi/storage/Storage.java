package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;

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

  StorageImplementation getImplementation();

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

  // Groups
  CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key);

  CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys);

  CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node);

  <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier);

  CompletableFuture<Collection<NodeGroup>> loadAllGroups();

  CompletableFuture<Void> saveGroup(NodeGroup group);

  CompletableFuture<Void> deleteGroup(NodeGroup group);

  // Find Data
  CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                            LocalDateTime time);

  CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key);

  CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer
  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
      PathVisualizer<T, ?, ?> visualizer);

  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
		  VisualizerType<T> type, NamespacedKey key);

  CompletableFuture<Collection<PathVisualizer<?, ?, ?>>> loadVisualizers();

  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<Map<NamespacedKey, T>> loadVisualizers(
      VisualizerType<T> type);

  <T extends PathVisualizer<T, D, ?>, D> CompletableFuture<Optional<T>> loadVisualizer(
      NamespacedKey key);

  CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?, ?> visualizer);

  CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?, ?> visualizer);
}