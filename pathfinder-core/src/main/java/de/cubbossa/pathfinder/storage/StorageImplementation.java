package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public interface StorageImplementation {

  void init() throws Exception;
  void shutdown();

  // Node Type
  void saveNodeType(UUID node, NodeType<?> type);
  void saveNodeTypes(Map<UUID, NodeType<?>> typeMapping);
  <N extends Node<N>> Optional<NodeType<N>> loadNodeType(UUID node);
  Map<UUID, NodeType<?>> loadNodeTypes(Collection<UUID> nodes);

  // Nodes
  <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location);
  <N extends Node<N>> Optional<N> loadNode(UUID id);
  <N extends Node<N>> Optional<N> loadNode(NodeType<N> type, UUID id);
  Collection<Node<?>> loadNodes();
  Collection<Node<?>> loadNodes(Collection<UUID> ids);
  <N extends Node<N>> void saveNode(N node);
  void deleteNodes(Collection<Node<?>> node);

  // Edges
  Edge createAndLoadEdge(UUID start, UUID end, double weight);
  Collection<Edge> loadEdgesFrom(UUID start);
  Collection<Edge> loadEdgesTo(UUID end);
  Optional<Edge> loadEdge(UUID start, UUID end);
  void saveEdge(Edge edge);
  void deleteEdge(Edge edge);

  // Waypoint
  Waypoint createAndLoadWaypoint(Location location);
  Optional<Waypoint> loadWaypoint(UUID uuid);
  Collection<Waypoint> loadAllWaypoints();
  Collection<Waypoint> loadWaypoints(Collection<UUID> uuids);
  void saveWaypoint(Waypoint waypoint);
  void deleteWaypoints(Collection<Waypoint> waypoints);

  // Groups
  NodeGroup createAndLoadGroup(NamespacedKey key);
  Optional<NodeGroup> loadGroup(NamespacedKey key);
  Collection<NodeGroup> loadGroups(Collection<NamespacedKey> key);
  Collection<NodeGroup> loadGroups(UUID node);
  <M extends Modifier> Collection<NodeGroup> loadGroups(Class<M> modifier);
  Collection<NodeGroup> loadAllGroups();
  Collection<Node<?>> loadGroupNodes(NodeGroup group);
  void saveGroup(NodeGroup group);
  void deleteGroup(NodeGroup group);

  // Find Data

  // Visualizer
}
