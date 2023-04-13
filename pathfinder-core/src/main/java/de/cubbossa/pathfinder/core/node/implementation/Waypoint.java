package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.WorldLocation;
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

  private final UUID nodeId;
  private final List<SimpleEdge> edges;
  private final Map<NamespacedKey, NodeGroup> groups;

  private WorldLocation location;

  public Waypoint(UUID databaseId) {
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
    return PathPlugin.getInstance().getWaypointNodeType();
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
