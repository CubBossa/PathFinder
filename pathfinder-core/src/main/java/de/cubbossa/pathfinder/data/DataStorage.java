package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;

public interface DataStorage extends
    NodeDataStorage<Waypoint> {

  default void connect() throws IOException {
    connect(() -> {
    });
  }

  /**
   * Sets up the database files or does nothing if the database is already setup.
   * If the database hasn't yet existed, the initial callback will be executed.
   *
   * @param initial A callback to be executed if the database was initially created
   */
  void connect(Runnable initial) throws IOException;

  void disconnect();


  Map<NamespacedKey, RoadMap> loadRoadMaps();

  void updateRoadMap(RoadMap roadMap);

  default void deleteRoadMap(RoadMap roadMap) {
    deleteRoadMap(roadMap.getKey());
  }

  void deleteRoadMap(NamespacedKey key);


  void saveEdges(Collection<Edge> edges);

  Collection<Edge> loadEdges(RoadMap roadMap, Map<Integer, Node<?>> scope);

  void deleteEdgesFrom(Node<?> start);

  void deleteEdgesTo(Node<?> end);

  void deleteEdges(Collection<Edge> edges);

  default void deleteEdge(Edge edge) {
    deleteEdge(edge.getStart(), edge.getEnd());
  }

  void deleteEdge(Node<?> start, Node<?> end);


  void assignNodesToGroup(NodeGroup group, NodeSelection selection);

  void removeNodesFromGroup(NodeGroup group, Iterable<Groupable<?>> selection);

  Map<Integer, ? extends Collection<NamespacedKey>> loadNodeGroupNodes();


  HashedRegistry<NodeGroup> loadNodeGroups();

  void updateNodeGroup(NodeGroup group);

  default void deleteNodeGroup(NodeGroup group) {
    deleteNodeGroup(group.getKey());
  }

  void deleteNodeGroup(NamespacedKey key);


  Map<NamespacedKey, Collection<String>> loadSearchTerms();

  void addSearchTerms(NodeGroup group, Collection<String> searchTerms);

  void removeSearchTerms(NodeGroup group, Collection<String> searchTerms);


  DiscoverInfo createDiscoverInfo(UUID player, Discoverable discoverable, LocalDateTime foundDate);

  Map<NamespacedKey, DiscoverInfo> loadDiscoverInfo(UUID playerId);

  void deleteDiscoverInfo(UUID playerId, NamespacedKey discoverKey);


  <T extends PathVisualizer<T, ?>> Map<NamespacedKey, T> loadPathVisualizer(VisualizerType<T> type);

  <T extends PathVisualizer<T, ?>> void updatePathVisualizer(T visualizer);

  void deletePathVisualizer(PathVisualizer<?, ?> visualizer);
}
