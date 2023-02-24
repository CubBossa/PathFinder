package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public interface NodeDataStorage<N extends Node<N>> {

  Map<Integer, N> loadNodes(RoadMap roadMap);

  void updateNode(N node);

  default void deleteNodes(Integer... nodeId) {
    deleteNodes(Arrays.asList(nodeId));
  }

  void deleteNodes(Collection<Integer> nodeIds);
}
