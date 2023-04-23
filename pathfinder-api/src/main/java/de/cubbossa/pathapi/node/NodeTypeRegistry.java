package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.Collection;

public interface NodeTypeRegistry {
  <N extends Node<N>> NodeType<N> getType(NamespacedKey key);

  Collection<NamespacedKey> getTypeKeys();

  Collection<NodeType<? extends Node<?>>> getTypes();

  <N extends Node<N>> void register(NodeType<N> type);

  <N extends Node<N>> void unregister(NodeType<N> type);

  void unregister(NamespacedKey key);
}
