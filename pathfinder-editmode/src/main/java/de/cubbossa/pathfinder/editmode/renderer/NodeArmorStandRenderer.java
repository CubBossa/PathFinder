package de.cubbossa.pathfinder.editmode.renderer;

import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.LEFT_CLICK_NODE;
import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.RIGHT_CLICK_NODE;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.editmode.utils.ItemStackUtils;
import de.cubbossa.pathfinder.editor.GraphRenderer;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class NodeArmorStandRenderer extends AbstractArmorstandRenderer<Node>
    implements GraphRenderer<Player> {

  private static final Vector NODE_OFFSET = new Vector(0, -1.75, 0);

  private final ItemStack nodeHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_GREEN);

  public NodeArmorStandRenderer(JavaPlugin plugin) {
    super(plugin);
    setRenderDistance(PathFinder.get().getConfiguration().getEditMode().getNodeArmorStandRenderDistance());
  }

  @Override
  CompletableFuture<Location> retrieveFrom(Node element) {
    return CompletableFuture.completedFuture(BukkitVectorUtils.toBukkit(element.getLocation()).add(NODE_OFFSET));
  }

  @Override
  Action<TargetContext<Node>> handleInteract(Player player, int slot, boolean left) {
    return left ? LEFT_CLICK_NODE : RIGHT_CLICK_NODE;
  }

  @Override
  boolean equals(Node a, Node b) {
    return a.getNodeId().equals(b.getNodeId());
  }

  @Override
  ItemStack head(Node element) {
    return nodeHead.clone();
  }

  @Override
  boolean isSmall(Node element) {
    return false;
  }

  @Nullable
  @Override
  Component getName(Node element) {
    return null;
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.runAsync(() -> {
      hideElements(entityNodeMap.values(), player.unwrap());
      players.remove(player);
    });
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      showElements(nodes, player.unwrap());
      players.add(player);
    });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> hideElements(nodes, player.unwrap()));
  }
}
