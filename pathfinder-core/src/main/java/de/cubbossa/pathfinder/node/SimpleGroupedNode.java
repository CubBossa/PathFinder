package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.GroupedNode;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.implementation.Waypoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public record SimpleGroupedNode(Node node, Collection<NodeGroup> groups) implements GroupedNode {
  @Override
  public GroupedNode merge(GroupedNode other) {
    Node node = new Waypoint(UUID.randomUUID());
    Collection<NodeGroup> groups = new HashSet<>(groups());
    groups.addAll(other.groups());
    return new SimpleGroupedNode(node, groups);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimpleGroupedNode that = (SimpleGroupedNode) o;
    return Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node);
  }
}
