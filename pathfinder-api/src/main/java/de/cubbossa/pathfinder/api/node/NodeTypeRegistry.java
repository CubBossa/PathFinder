package de.cubbossa.pathfinder.api.node;

import de.cubbossa.pathfinder.api.misc.KeyedRegistry;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

public class NodeTypeRegistry {

  private final KeyedRegistry<NodeType<? extends Node<?>>> types;
  @Getter
  @Setter
  private NodeType<Waypoint> waypointNodeType;

  public NodeTypeRegistry() {
    this.types = new HashedRegistry<>();
  }

  public <N extends Node<N>> NodeType<N> getNodeType(NamespacedKey key) {
    return (NodeType<N>) types.get(key);
  }

  public Collection<NamespacedKey> getTypeKeys() {
    return types.keySet();
  }

  public Collection<de.cubbossa.pathfinder.api.node.NodeType<? extends Node<?>>> getTypes() {
    return types.values();
  }

  public <N extends Node<N>> void registerNodeType(NodeType<N> type) {
    this.types.put(type);
  }

  public <N extends Node<N>> void unregister(de.cubbossa.pathfinder.api.node.NodeType<N> type) {
    unregister(type.getKey());
  }

  public void unregister(NamespacedKey key) {
    this.types.remove(key);
  }
}
