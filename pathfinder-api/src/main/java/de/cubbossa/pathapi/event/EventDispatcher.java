package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.function.Consumer;

public interface EventDispatcher {

  <N extends Node<N>> void dispatchNodeCreate(N node);
  void dispatchNodeSave(Node<?> node);
  void dispatchNodeLoad(Node<?> node);
  <N extends Node<N>> void dispatchNodeDelete(N node);
  void dispatchNodesDelete(Collection<Node<?>> nodes);
  void dispatchNodeUnassign(Node<?> node, Collection<NodeGroup> groups);
  void dispatchNodeAssign(Node<?> node, Collection<NodeGroup> groups);

  void dispatchGroupCreate(NodeGroup group);
  void dispatchGroupDelete(NodeGroup group);

  <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event);

  void drop(Listener<?> listener);
}
