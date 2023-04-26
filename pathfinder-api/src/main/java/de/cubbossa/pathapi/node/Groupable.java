package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.Collection;

public interface Groupable extends Node {

  Collection<NodeGroup> getGroups();

  void addGroup(NodeGroup group);

  void removeGroup(NamespacedKey group);

  void clearGroups();
}
