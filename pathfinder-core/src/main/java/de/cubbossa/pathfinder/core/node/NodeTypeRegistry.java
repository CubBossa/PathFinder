package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;

public class NodeTypeRegistry {

  private final HashedRegistry<de.cubbossa.pathfinder.api.node.NodeType<? extends Node<?>>> types;
  @Getter
  @Setter
  private de.cubbossa.pathfinder.api.node.NodeType<Waypoint> waypointNodeType;

  public NodeTypeRegistry() {
    this.types = new HashedRegistry<>();
  }

  public <N extends Node<N>> de.cubbossa.pathfinder.api.node.NodeType<N> getNodeType(NamespacedKey key) {
    return types.get(key);
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
