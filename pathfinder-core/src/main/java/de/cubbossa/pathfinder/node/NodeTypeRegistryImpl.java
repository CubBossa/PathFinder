package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;

public class NodeTypeRegistryImpl implements NodeTypeRegistry {

  private final KeyedRegistry<NodeType<? extends Node>> types;

  public NodeTypeRegistryImpl() {
    this.types = new HashedRegistry<>();
  }

  @Override
  public <N extends Node> NodeType<N> getType(NamespacedKey key) {
    return (NodeType<N>) types.get(key);
  }

  @Override
  public Collection<NamespacedKey> getTypeKeys() {
    return types.keySet();
  }

  @Override
  public Collection<NodeType<? extends Node>> getTypes() {
    return types.values();
  }

  @Override
  public <N extends Node> void register(NodeType<N> type) {
    this.types.put(type);
  }

  @Override
  public <N extends Node> void unregister(NodeType<N> type) {
    unregister(type.getKey());
  }

  @Override
  public void unregister(NamespacedKey key) {
    this.types.remove(key);
  }
}
