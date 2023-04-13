package de.cubbossa.pathfinder.api.node;

import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import java.util.Collection;

public interface Groupable<N extends Node<N>> extends Node<N> {

  Collection<NodeGroup> getGroups();

  void addGroup(NodeGroup group);

  void removeGroup(NamespacedKey group);

  void clearGroups();
}
