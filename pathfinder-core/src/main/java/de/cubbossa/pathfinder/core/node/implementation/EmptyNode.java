package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyNode implements Node<EmptyNode> {

  public static final NodeType<EmptyNode> TYPE = new NodeType<>(
      new NamespacedKey(PathPlugin.getInstance(), "empty"),
      "empty",
      new ItemStack(Material.DIRT)
  ) {
    @Override
    public EmptyNode createNode(NodeCreationContext context) {
      throw new IllegalStateException("EmptyNode are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  @Getter
  private final Location location;

  public EmptyNode(World world) {
    this.location = new Location(world, 0, 0, 0);
  }

  @Override
  public NodeType<EmptyNode> getType() {
    return TYPE;
  }

  @Override
  public boolean isPersistent() {
    return false;
  }

  @Override
  public Collection<SearchTerm> getSearchTerms() {
    return new HashSet<>();
  }

  @Override
  public Collection<Node<?>> getGroup() {
    return new HashSet<>();
  }

  @Override
  public int getNodeId() {
    return -1;
  }

  @Override
  public void setLocation(Location location) {

  }

  @Override
  public Collection<Edge> getEdges() {
    return new HashSet<>();
  }

  @Override
  public @Nullable Double getCurveLength() {
    return null;
  }

  @Override
  public void setCurveLength(Double value) {

  }

  @Override
  public Edge connect(Node<?> target) {
    return null;
  }

  @Override
  public void disconnect(Node<?> target) {

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
