package de.cubbossa.pathfinder.api.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.storage.NodeDataStorage;
import org.bukkit.Keyed;

public interface NodeType<N extends Node<N>> extends Keyed, Named, NodeDataStorage<N> {
  NodeDataStorage<N> getStorage();
}
