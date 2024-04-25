package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GroupedNodeImpl implements GroupedNode {

  private final Node node;
  private final Collection<NodeGroup> groups;

  public GroupedNodeImpl(Node node, Collection<NodeGroup> groups) {
    this.node = node;
    this.groups = new LinkedList<>(groups);
  }

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
    if (o == null || getClass() != o.getClass()) {
      if (o instanceof Node node) {
        return Objects.equals(this.node, node);
      } else return false;
    }
    GroupedNodeImpl that = (GroupedNodeImpl) o;
    return Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return node.hashCode();
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

  @Override
  public Node node() {
    return node;
  }

  @Override
  public Collection<NodeGroup> groups() {
    return groups;
  }

  @Override
  public String toString() {
    return "GroupedNodeImpl[" +
        "node=" + node + ", " +
        "groups=" + groups + ']';
  }

}
