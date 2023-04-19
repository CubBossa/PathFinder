package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.api.misc.KeyedRegistry;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

public class NodeTypeRegistry implements de.cubbossa.pathfinder.api.node.NodeTypeRegistry {

  private final KeyedRegistry<NodeType<? extends Node<?>>> types;
  @Getter
  @Setter
  private NodeType<Waypoint> waypointNodeType;

  public NodeTypeRegistry() {
    this.types = new HashedRegistry<>();
  }

  @Override
  public <N extends Node<N>> NodeType<N> getType(NamespacedKey key) {
    return (NodeType<N>) types.get(key);
  }

  @Override
  public Collection<NamespacedKey> getTypeKeys() {
    return types.keySet();
  }

  @Override
  public Collection<de.cubbossa.pathfinder.api.node.NodeType<? extends Node<?>>> getTypes() {
    return types.values();
  }

  @Override
  public <N extends Node<N>> void register(NodeType<N> type) {
    this.types.put(type);
  }

  @Override
  public <N extends Node<N>> void unregister(de.cubbossa.pathfinder.api.node.NodeType<N> type) {
    unregister(type.getKey());
  }

  @Override
  public void unregister(NamespacedKey key) {
    this.types.remove(key);
  }
}
