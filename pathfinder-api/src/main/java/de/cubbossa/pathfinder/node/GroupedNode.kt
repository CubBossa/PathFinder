package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.group.NodeGroup;
import java.util.Collection;

public interface GroupedNode extends Node {

  Node node();

  Collection<NodeGroup> groups();

  GroupedNode merge(GroupedNode other);
}
