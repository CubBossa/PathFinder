package de.cubbossa.pathfinder.api.event;

import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface EventDispatcher {
  <N extends Node<N>> void dispatchNodeCreate(N node);

  void dispatchSaveNode(Node<?> node);

  void dispatchLoadNode(Node<?> node);

  <N extends Node<N>> void dispatchNodeDelete(N node);

  void dispatchNodesDelete(Collection<Node<?>> nodes);

  void dispatchNodeUnassign(Node<?> node, Collection<NodeGroup> groups);

  void dispatchNodeAssign(Node<?> node, Collection<NodeGroup> groups);

  <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event);

  void drop(Listener<?> listener);
}
