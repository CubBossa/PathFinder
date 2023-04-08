package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.util.*;

@Getter
@Setter
public class Waypoint implements Node<Waypoint>, Groupable<Waypoint> {

  private final UUID nodeId;
  private final List<Edge> edges;
  private final Map<NamespacedKey, NodeGroup> groups;

  private Location location;

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
