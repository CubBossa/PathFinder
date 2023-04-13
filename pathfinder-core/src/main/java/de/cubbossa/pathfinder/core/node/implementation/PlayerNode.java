package de.cubbossa.pathfinder.core.node.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.node.AbstractNodeType;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerNode implements Node<PlayerNode> {

  public static final de.cubbossa.pathfinder.api.node.NodeType<PlayerNode> TYPE = new AbstractNodeType<>(
      new NamespacedKey(PathPlugin.getInstance(), "empty"),
      "empty",
      new ItemStack(Material.DIRT),
      PathPlugin.getInstance().getMiniMessage()
  ) {
    @Override
    public PlayerNode createAndLoadNode(Context context) {
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
  public de.cubbossa.pathfinder.api.node.NodeType<PlayerNode> getType() {
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
  public Collection<SimpleEdge> getEdges() {
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
