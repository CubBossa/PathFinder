package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Getter
public class PlayerNode implements Groupable {

  public static final NodeType<PlayerNode> TYPE = new AbstractNodeType<>(
      CommonPathFinder.pathfinder("player"),
      "empty",
      PathFinderProvider.get().getMiniMessage()
  ) {
    @Override
    public PlayerNode createAndLoadNode(Context context) {
      throw new IllegalStateException("PlayerNodes are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final PathPlayer<?> player;
  private final Collection<NodeGroup> groups;

  public PlayerNode(PathPlayer<?> player) {
    this.player = player;
    this.groups = new HashSet<>();
  }

  @Override
  public UUID getNodeId() {
    return player.getUniqueId();
  }

  @Override
  public Location getLocation() {
    return player.getLocation();
  }

  @Override
  public void setLocation(Location location) {

  }

  @Override
  public Collection<Edge> getEdges() {
    return new HashSet<>();
  }

  @Override
  public Optional<Edge> connect(UUID other, double weight) {
    return Optional.empty();
  }

  @Override
  public int compareTo(@NotNull Node o) {
    return 0;
  }

  @Override
  public String toString() {
    return "PlayerNode{" +
        "player=" + player.getName() +
        '}';
  }

  @Override
  public Collection<NodeGroup> getGroups() {
    return new HashSet<>(groups);
  }

  @Override
  public void addGroup(NodeGroup group) {
    this.groups.add(group);
  }

  @Override
  public void removeGroup(NamespacedKey group) {
    this.groups.removeIf(g -> g.getKey().equals(group));
  }

  @Override
  public void clearGroups() {
    this.groups.clear();
  }
}
