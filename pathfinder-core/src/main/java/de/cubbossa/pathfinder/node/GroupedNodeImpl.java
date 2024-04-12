package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GroupedNodeImpl(Node node, Collection<NodeGroup> groups) implements GroupedNode {
  @Override
  public GroupedNode merge(GroupedNode other) {
    Node node = new Waypoint(UUID.randomUUID());
    Collection<NodeGroup> groups = new HashSet<>(groups());
    groups.addAll(other.groups());
    return new GroupedNodeImpl(node, groups);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GroupedNodeImpl that = (GroupedNodeImpl) o;
    return Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node);
  }

  @Override
  public UUID getNodeId() {
    return node.getNodeId();
  }

  @Override
  public Location getLocation() {
    return node.getLocation();
  }

  @Override
  public void setLocation(Location location) {
    node.setLocation(location);
  }

  @Override
  public Changes<Edge> getEdgeChanges() {
    return node.getEdgeChanges();
  }

  @Override
  public Collection<Edge> getEdges() {
    return node.getEdges();
  }

  @Override
  public Optional<Edge> connect(UUID other, double weight) {
    return node.connect(other, weight);
  }

  @Override
  public Node clone() {
    return new GroupedNodeImpl(node.clone(), groups);
  }

  @Override
  public Node clone(UUID id) {
    return new GroupedNodeImpl(node.clone(id), groups);
  }
}
