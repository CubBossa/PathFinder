package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

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
    }, BukkitPathFinder.mainThreadExecutor());
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      showElements(nodes, player.unwrap());
      players.add(player);
    }, BukkitPathFinder.mainThreadExecutor());
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      hideElements(nodes, player.unwrap());
    }, BukkitPathFinder.mainThreadExecutor());
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
  void render(Node element, BlockDisplay entity) {
    entity.setBlock(Material.LIME_CONCRETE.createBlockData());
    Transformation t = entity.getTransformation();
    t.getTranslation().sub(new Vector3f(NODE_SCALE, NODE_SCALE, NODE_SCALE).mul(0.5f));
    t.getScale().set(NODE_SCALE);
    entity.setTransformation(t);

    Interaction interaction = entity.getWorld().spawn(entity.getLocation(), Interaction.class);
    interaction.setInteractionWidth(NODE_SCALE);
    interaction.setInteractionHeight(NODE_SCALE);
    entity.getPassengers().forEach(Entity::remove);
    entity.addPassenger(interaction);
  }
}
