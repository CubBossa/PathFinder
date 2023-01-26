package de.cubbossa.pathfinder.core.node;

import java.util.Collection;

public interface Groupable extends Node {

  Collection<NodeGroup> getGroups();

  void addGroup(NodeGroup group);

  void removeGroup(NodeGroup group);

  void clearGroups();
}
