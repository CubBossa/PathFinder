package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;

public class NodeTypeRegistryImpl implements NodeTypeRegistry {

  public final ExtensionPoint<NodeType> EXTENSION_POINT = new ExtensionPoint<>(NodeType.class);

  private final KeyedRegistry<NodeType<?>> types;

  public NodeTypeRegistryImpl(PathFinder pathFinder) {
    this.types = new HashedRegistry<>();
    pathFinder.getDisposer().register(pathFinder, this);

    EXTENSION_POINT.getExtensions().forEach(this::register);
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
    public Collection<NodeType<?>> getTypes() {
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
