package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.World;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class EmptyNode implements Node {

  public static final NodeType<EmptyNode> TYPE = new AbstractNodeType<>(
      AbstractPathFinder.pathfinder("empty")
  ) {

    @Override
    public EmptyNode createNodeInstance(Context context) {
      return new EmptyNode(context.id(), context.location().getWorld());
    }

    @Override
    public boolean canBeCreated(Context context) {
      return false;
    }

    @Override
    public EmptyNode createAndLoadNode(Context context) {
      throw new IllegalStateException("EmptyNode are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final UUID uuid;
  @Getter
  private final Location location;
  @Getter
  private final Changes<Edge> edgeChanges = new Changes<>();

  public EmptyNode(World world) {
    this(UUID.randomUUID(), world);
  }

  public EmptyNode(UUID id, World world) {
    this.uuid = id;
    this.location = new Location(0, 0, 0, world);
  }

  @Override
  public UUID getNodeId() {
    return uuid;
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
  public Node clone(UUID id) {
    try {
      return (Node) super.clone();
    } catch (CloneNotSupportedException e) {
      return new EmptyNode(uuid, location.getWorld());
    }
  }

  @Override
  public Node clone() {
    return clone(uuid);
  }

  @Override
  public String toString() {
    return "EmptyNode{}";
  }
}
