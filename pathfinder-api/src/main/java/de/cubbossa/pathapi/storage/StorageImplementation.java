package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public interface StorageImplementation {

  void init() throws Exception;
  void shutdown();

  void setLogger(Logger logger);
  Logger getLogger();

  // Node Type
  void saveNodeType(UUID node, NodeType<? extends Node<?>> type);
  void saveNodeTypes(Map<UUID, NodeType<? extends Node<?>>> typeMapping);
  <N extends Node<N>> Optional<NodeType<N>> loadNodeType(UUID node);
  Map<UUID, NodeType<? extends Node<?>>> loadNodeTypes(Collection<UUID> nodes);

  // Nodes
  <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location);
  <N extends Node<N>> Optional<N> loadNode(UUID id);
  Collection<Node<?>> loadNodes();
  Collection<Node<?>> loadNodes(Collection<UUID> ids);
  void saveNode(Node<?> node);
  void deleteNodes(Collection<Node<?>> node);

  // Edges
  Edge createAndLoadEdge(UUID start, UUID end, double weight);
  Collection<Edge> loadEdgesFrom(UUID start);
  Collection<Edge> loadEdgesTo(UUID end);
  Optional<Edge> loadEdge(UUID start, UUID end);
  void saveEdge(Edge edge);
  void deleteEdge(Edge edge);

  // Groups
  NodeGroup createAndLoadGroup(NamespacedKey key);
  Optional<NodeGroup> loadGroup(NamespacedKey key);
  Collection<NodeGroup> loadGroups(Collection<NamespacedKey> key);
  List<NodeGroup> loadGroups(Pagination pagination);
  Collection<NodeGroup> loadGroups(UUID node);
  <M extends Modifier> Collection<NodeGroup> loadGroups(Class<M> modifier);
  Collection<NodeGroup> loadAllGroups();
  Collection<UUID> loadGroupNodes(NodeGroup group);
  void saveGroup(NodeGroup group);
  void deleteGroup(NodeGroup group);

  // Find Data
  DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time);
  Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key);
  void deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer
  <T extends PathVisualizer<T, ?, ?>> T createAndLoadVisualizer(VisualizerType<T> type, NamespacedKey key);
  <T extends PathVisualizer<T, ?, ?>> Map<NamespacedKey, T> loadVisualizers(VisualizerType<T> type);
  <T extends PathVisualizer<T, ?, ?>> Optional<T> loadVisualizer(VisualizerType<T> type, NamespacedKey key);
  void saveVisualizer(PathVisualizer<?, ?, ?> visualizer);
  void deleteVisualizer(PathVisualizer<?, ?, ?> visualizer);
}
