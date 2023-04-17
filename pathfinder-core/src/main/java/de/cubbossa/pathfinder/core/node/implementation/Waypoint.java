package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.Location;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.api.node.Groupable;
import de.cubbossa.pathfinder.api.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Waypoint implements Node<Waypoint>, Groupable<Waypoint> {

  private final NodeType<Waypoint> type;
  private final UUID nodeId;
  private final List<Edge> edges;
  private final Map<NamespacedKey, NodeGroup> groups;

  private Location location;

  public Waypoint(NodeType<Waypoint> type, UUID databaseId) {
    this.type = type;
    this.nodeId = databaseId;
    this.groups = new HashMap<>();

    edges = new ArrayList<>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Waypoint waypoint)) {
      return false;
    }

    return nodeId == waypoint.nodeId;
  }

  @Override
  public NodeType<Waypoint> getType() {
    return type;
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
}
