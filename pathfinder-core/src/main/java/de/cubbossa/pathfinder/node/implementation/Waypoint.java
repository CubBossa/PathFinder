package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathapi.Changes;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.util.ModifiedHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class Waypoint implements Node {

  private final UUID nodeId;
  private final ModifiedHashSet<Edge> edges;
  private final Collection<NodeGroup> groups;

  private Location location;

  public Waypoint(UUID databaseId) {
    this.nodeId = databaseId;
    this.groups = new HashSet<>();

    edges = new ModifiedHashSet<>();
  }

  @Override
  public Changes<Edge> getEdgeChanges() {
    return edges.getChanges();
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
    return new HashSet<>(groups);
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
  public Waypoint clone(UUID uuid) {
    Waypoint clone = new Waypoint(uuid);
    clone.location = location.clone();
    clone.edges.addAll(this.edges);
    clone.groups.addAll(groups);
    return clone;
  }

  @Override
  public Waypoint clone() {
    try {
      return (Waypoint) super.clone();
    } catch (CloneNotSupportedException e) {
      return clone(nodeId);
    }
  }
}
