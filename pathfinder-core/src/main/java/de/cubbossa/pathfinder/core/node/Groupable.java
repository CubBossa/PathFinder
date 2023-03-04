package de.cubbossa.pathfinder.core.node;

import java.util.Collection;

public interface Groupable<N extends Node<N>> extends Node<N> {

  Collection<NodeGroup> getGroups();

  void addGroup(NodeGroup group);

  void removeGroup(NodeGroup group);

  void clearGroups();
}
