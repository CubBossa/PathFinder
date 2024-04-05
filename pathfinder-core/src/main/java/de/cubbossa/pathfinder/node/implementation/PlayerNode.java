package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathapi.Changes;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class PlayerNode implements Node {

  public static final NodeType<PlayerNode> TYPE = new AbstractNodeType<>(
      AbstractPathFinder.pathfinder("player")
  ) {
    @Override
    public PlayerNode createAndLoadNode(Context context) {
      throw new IllegalStateException("PlayerNodes are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final PathPlayer<?> player;
  private final Changes<Edge> edgeChanges = new Changes<>();

  public PlayerNode(PathPlayer<?> player) {
    this.player = player;
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
  public Node clone() {
    try {
      return (Node) super.clone();
    } catch (CloneNotSupportedException e) {
      return new PlayerNode(player);
    }
  }

  @Override
  public Node clone(UUID id) {
    throw new IllegalStateException("Cannot clone a player node with ID parameter.");
  }

  @Override
  public String toString() {
    return "PlayerNode{" +
        "player=" + player.getName() +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlayerNode that = (PlayerNode) o;
    return Objects.equals(getPlayer().getUniqueId(), that.getPlayer().getUniqueId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPlayer().getUniqueId());
  }
}
