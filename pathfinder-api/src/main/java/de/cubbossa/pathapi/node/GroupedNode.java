package de.cubbossa.pathapi.node;

import de.cubbossa.pathapi.group.NodeGroup;

import java.util.Collection;

public interface GroupedNode {

  Node node();

  Collection<NodeGroup> groups();
}
