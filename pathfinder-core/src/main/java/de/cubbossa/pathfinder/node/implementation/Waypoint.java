package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.SimpleEdge;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Waypoint implements Node, Groupable {

  private final UUID nodeId;
  private final HashSet<Edge> edges;
  private final Map<NamespacedKey, NodeGroup> groups;

  private Location location;

  public Waypoint(UUID databaseId) {
    this.nodeId = databaseId;
    this.groups = new HashMap<>();

    edges = new HashSet<>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Waypoint waypoint)) {
      return false;
    }
    return nodeId.equals(waypoint.nodeId);
  }

  @Override
  public int hashCode() {
    return nodeId.hashCode();
  }

  public Collection<NodeGroup> getGroups() {
    return new HashSet<>(groups.values());
  }

  @Override
  public void addGroup(NodeGroup group) {
    groups.put(group.getKey(), group);
  }

  @Override
  public void removeGroup(NamespacedKey group) {
    groups.remove(group);
  }

  @Override
  public void clearGroups() {
    groups.clear();
  }

  @Override
  public String toString() {
    return "Waypoint{" +
        "nodeId=" + nodeId +
        ", location=" + location +
        '}';
  }

  @Override
  public Optional<Edge> connect(UUID other, double weight) {
    if (getConnection(other).isPresent()) {
      return Optional.empty();
    }
    Edge e = new SimpleEdge(nodeId, other, (float) weight);
    edges.add(e);
    return Optional.of(e);
  }

  @Override
  public Waypoint clone() {
    try {
      return (Waypoint) super.clone();
    } catch (CloneNotSupportedException e) {
      Waypoint clone = new Waypoint(nodeId);
      clone.location = location.clone();
      clone.edges.addAll(this.edges);
      clone.groups.putAll(groups);
      return clone;
    }
  }
}
