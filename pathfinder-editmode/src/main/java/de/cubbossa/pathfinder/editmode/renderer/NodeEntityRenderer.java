package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.LEFT_CLICK_NODE;
import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.RIGHT_CLICK_NODE;

public class NodeEntityRenderer extends AbstractEntityRenderer<Node, BlockDisplay> {


  private static final float NODE_SCALE = .4f;

  public NodeEntityRenderer(JavaPlugin plugin) {
    super(plugin, BlockDisplay.class);
    setRenderDistance(PathFinderProvider.get().getConfiguration().getEditMode().getNodeArmorStandRenderDistance());
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.runAsync(() -> {
      hideElements(entityNodeMap.values(), player.unwrap());
      players.remove(player);
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      showElements(nodes, player.unwrap()).join();
      players.add(player);
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      hideElements(nodes, player.unwrap());
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  boolean equals(Node a, Node b) {
    return a.equals(b);
  }

  @Override
  CompletableFuture<Location> location(Node element) {
    return CompletableFuture.completedFuture(BukkitVectorUtils.toBukkit(element.getLocation()));
  }

  @Override
  Action<TargetContext<Node>> handleInteract(Player player, int slot, boolean left) {
    return left ? LEFT_CLICK_NODE : RIGHT_CLICK_NODE;
  }

  @Override
  void render(Node element, BlockDisplay entity) {
    entity.setBlock(Material.LIME_CONCRETE.createBlockData());
    Transformation t = entity.getTransformation();
    t.getTranslation().sub(new Vector3f(NODE_SCALE, NODE_SCALE, NODE_SCALE).mul(0.5f));
    t.getScale().set(NODE_SCALE);
    entity.setTransformation(t);
  }

  @Override
  void hitbox(Node element, Interaction entity) {
    entity.setInteractionWidth(NODE_SCALE);
    entity.setInteractionHeight(NODE_SCALE);
    entity.teleport(entity.getLocation().subtract(0, NODE_SCALE / 2., 0));
  }
}
