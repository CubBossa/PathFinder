package de.cubbossa.pathfinder.node.implementation;

import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import de.cubbossa.pathfinder.util.WorldImpl;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EmptyNode implements Node<EmptyNode> {

  public static final NodeType<EmptyNode> TYPE = new AbstractNodeType<>(
      PathPlugin.pathfinder("empty"),
      "empty",
      new ItemStack(Material.DIRT),
      PathPlugin.getInstance().getMiniMessage()
  ) {

    @Override
    public EmptyNode createAndLoadNode(Context context) {
      throw new IllegalStateException("EmptyNode are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final UUID uuid = UUID.randomUUID();
  @Getter
  private final Location location;

  public EmptyNode(World world) {
    this.location = new Location(0, 0, 0, new WorldImpl(world.getUID()));
  }

  @Override
  public NodeType<EmptyNode> getType() {
    return TYPE;
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
  public int compareTo(@NotNull Node o) {
    return 0;
  }

  @Override
  public String toString() {
    return "EmptyNode{}";
  }
}
