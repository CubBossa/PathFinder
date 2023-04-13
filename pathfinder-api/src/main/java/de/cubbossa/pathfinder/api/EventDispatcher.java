package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.UUID;

public interface EventDispatcher {
  <N extends Node<N>> void dispatchNodeCreate(N node);

  <N extends Node<N>> void dispatchNodeDelete(Node<?> node);

  <N extends Node<N>> void dispatchNodesDelete(Collection<UUID> nodes);

  void dispatchNodeUnassign(Node<?> node, Collection<NodeGroup> groups);

  void dispatchNodeAssign(Node<?> node, Collection<NodeGroup> groups);
}
