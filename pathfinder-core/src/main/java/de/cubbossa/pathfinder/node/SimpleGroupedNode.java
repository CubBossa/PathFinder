package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;

import java.util.Collection;

public record SimpleGroupedNode(Node node, Collection<NodeGroup> groups) implements GroupedNode {
}
