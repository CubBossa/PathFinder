package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.NamespacedKey;

import java.util.Collection;

public interface NodeTypeRegistry {
  <N extends Node> NodeType<N> getType(NamespacedKey key);

  Collection<NamespacedKey> getTypeKeys();

    Collection<NodeType<?>> getTypes();

  <N extends Node> void register(NodeType<N> type);

  <N extends Node> void unregister(NodeType<N> type);

  void unregister(NamespacedKey key);
}
