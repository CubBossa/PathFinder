package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Named;
import de.cubbossa.pathapi.storage.NodeDataStorage;

public interface NodeType<N extends Node<N>> extends Keyed, Named, NodeDataStorage<N> {
  NodeDataStorage<N> getStorage();
}
