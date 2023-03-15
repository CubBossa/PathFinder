package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public interface NodeDataStorage<N extends Node<N>> {

  Map<Integer, N> loadNodes(Collection<NodeGroup> withGroups);

  void updateNode(N node);

  default void deleteNodes(Integer... nodeId) {
    deleteNodes(Arrays.asList(nodeId));
  }

  void deleteNodes(Collection<Integer> nodeIds);
}
