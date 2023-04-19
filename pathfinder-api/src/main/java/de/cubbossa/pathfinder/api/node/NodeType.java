package de.cubbossa.pathfinder.api.node;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.Named;
import de.cubbossa.pathfinder.api.storage.NodeDataStorage;

public interface NodeType<N extends Node<N>> extends Keyed, Named, NodeDataStorage<N> {
  NodeDataStorage<N> getStorage();
}
