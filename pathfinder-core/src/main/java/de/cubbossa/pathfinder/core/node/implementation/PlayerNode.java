package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.query.SearchTerm;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
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
      new ItemStack(Material.DIRT),
      PathPlugin.getInstance().getMiniMessage()
  ) {
    @Override
    public CompletableFuture<PlayerNode> createNodeInStorage(NodeCreationContext context) {
      throw new IllegalStateException("PlayerNodes are only part of runtime navigation and "
          + "must be created from constructor.");
    }
  };

  private final Player player;

  public PlayerNode(Player player) {
    this.player = player;
  }

  @Override
  public UUID getNodeId() {
    return player.getUniqueId();
  }

  @Override
  public NodeType<PlayerNode> getType() {
    return TYPE;
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
