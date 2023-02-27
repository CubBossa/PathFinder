package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerNode implements Node<PlayerNode> {

  public static final NodeType<PlayerNode> TYPE = new NodeType<>(
      new NamespacedKey(PathPlugin.getInstance(), "empty"),
      "empty",
      new ItemStack(Material.DIRT)
  ) {
    @Override
    public PlayerNode createNode(NodeCreationContext context) {
      throw new IllegalStateException("PlayerNodes are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final Player player;
  private final RoadMap roadMap;

  public PlayerNode(Player player, RoadMap roadMap) {
    this.player = player;
    this.roadMap = roadMap;
  }

  @Override
  public NodeType<PlayerNode> getType() {
    return TYPE;
  }

  @Override
  public boolean isPersistent() {
    return false;
  }

  @Override
  public Collection<Node<?>> getGroup() {
    return new HashSet<>();
  }

  @Override
  public Collection<SearchTerm> getSearchTerms() {
    return null;
  }

  @Override
  public int getNodeId() {
    return -1;
  }

  @Override
  public NamespacedKey getRoadMapKey() {
    return roadMap.getKey();
  }

  @Override
  public Location getLocation() {
    return player.getLocation().add(0, .5f, 0);
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
  public int compareTo(@NotNull Node<?> o) {
    return 0;
  }

  @Override
  public String toString() {
    return "PlayerNode{" +
        "player=" + player.getName() +
        '}';
  }
}
