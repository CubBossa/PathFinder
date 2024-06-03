package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerNode implements Node {

  public static final NodeType<PlayerNode> TYPE = new AbstractNodeType<>(
      AbstractPathFinder.pathfinder("player")
  ) {

    @Override
    public boolean canBeCreated(Context context) {
      return false;
    }

    @Override
    public PlayerNode createNodeInstance(Context context) {
      throw new IllegalStateException("PlayerNodes are only part of runtime navigation and "
          + "must be created from constructor.");
    }

    @Override
    public @Nullable PlayerNode createAndLoadNode(Context context) {
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
    if (o == null || getClass() != o.getClass()) {
      if (o instanceof Node node) {
        return node.getNodeId().equals(getNodeId());
      }
      return false;
    }
    PlayerNode that = (PlayerNode) o;
    return Objects.equals(getPlayer().getUniqueId(), that.getPlayer().getUniqueId());
  }

  @Override
  public int hashCode() {
    return getPlayer().getUniqueId().hashCode();
  }
}
