package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * A {@link StorageImplementation} handles the actual serializing and deserializing of the given objects.
 * To access pathfinder data, use an instance of {@link Storage} instead, which also handles caching and
 * combines different loading methods (e.g. loading a node, its edges and its groups) into one.
 */
public interface StorageImplementation {

  /**
   * Initializes this storage implementation. Will be called by {@link Storage#init()} and will create
   * necessary files or objects. It assures that the instance can be used without issues afterward.
   *
   * @throws Exception Might call an exception. Not specified due to different implementations.
   */
  void init() throws Exception;

  void shutdown();

  Logger getLogger();

  void setLogger(Logger logger);

  // Node Type
  void saveNodeType(UUID node, NodeType<?> type);

  void saveNodeTypes(Map<UUID, NodeType<?>> typeMapping);

  <N extends Node> Optional<NodeType<N>> loadNodeType(UUID node);

  Map<UUID, NodeType<?>> loadNodeTypes(Collection<UUID> nodes);

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

  void assignToGroups(Collection<NodeGroup> groups, Collection<UUID> nodes);

  void unassignFromGroups(Collection<NodeGroup> groups, Collection<UUID> nodes);

  <M extends Modifier> void assignNodeGroupModifier(NamespacedKey group, M modifier);

  <M extends Modifier> void unassignNodeGroupModifier(NamespacedKey group, Class<M> modifier);

  // Find Data
  DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time);

  Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key);

  void deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer

  <VisualizerT extends PathVisualizer<?, ?>> void saveVisualizerType(NamespacedKey key,
                                                                     VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> loadVisualizerType(
      NamespacedKey key);

  Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypes(Collection<NamespacedKey> key);

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadVisualizer(VisualizerType<VisualizerT> type,
                                                                                 NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadVisualizers(
      VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadVisualizer(NamespacedKey key);

  <VisualizerT extends PathVisualizer<?, ?>> void saveVisualizer(VisualizerT visualizer);

  <VisualizerT extends PathVisualizer<?, ?>> void deleteVisualizer(VisualizerT visualizer);
}
