package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import org.bukkit.NamespacedKey;

public class NodeTypeRegistry {

  private final HashedRegistry<NodeType<?>> types;

  public NodeTypeRegistry() {
    this.types = new HashedRegistry<>();
  }

  public <N extends Node<N>> NodeType<N> getNodeType(NamespacedKey key) {
    return (NodeType<N>) types.get(key);
  }

  public Collection<NamespacedKey> getTypeKeys() {
    return types.keySet();
  }

  public Collection<NodeType<?>> getTypes() {
    return types.values();
  }

  public <N extends Node<N>> void registerNodeType(NodeType<N> type) {
    this.types.put(type);
  }

  public <N extends Node<N>> void unregister(NodeType<N> type) {
    unregister(type.getKey());
  }

  public void unregister(NamespacedKey key) {
    this.types.remove(key);
  }
}
