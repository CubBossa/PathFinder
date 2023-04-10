package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.util.Collection;
import org.bukkit.NamespacedKey;

public interface Groupable<N extends Node<N>> extends Node<N> {

  Collection<NodeGroup> getGroups();

  void addGroup(NodeGroup group);

  void removeGroup(NamespacedKey group);

  void clearGroups();
}
