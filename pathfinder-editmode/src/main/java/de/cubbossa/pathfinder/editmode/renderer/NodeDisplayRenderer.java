package de.cubbossa.pathfinder.editmode.renderer;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NodeDisplayRenderer implements GraphRenderer<Player> {

  record NodeDisplay(BlockDisplay entity, Collection<Player> players) {
  }

  private final Map<UUID, NodeDisplay> rendered;

  public NodeDisplayRenderer() {
    rendered = new HashMap<>();
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.runAsync(() -> {
      Player p = player.unwrap();
      rendered.forEach((uuid, nodeDisplay) -> eraseNode(p, uuid));
    }, BukkitPathFinder.mainThreadExecutor());
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      Player p = player.unwrap();
      nodes.forEach(node -> renderNode(p, node));
    }, BukkitPathFinder.mainThreadExecutor());
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      Player p = player.unwrap();
      nodes.forEach(node -> eraseNode(p, node.getNodeId()));
    }, BukkitPathFinder.mainThreadExecutor());
  }

  private void eraseNode(Player player, UUID node) {
    NodeDisplay nodeDisplay = rendered.get(node);
    if (nodeDisplay == null) {
      return;
    }
    if (nodeDisplay.players().size() > 1) {
      // player.hideEntity(PathFinderPlugin.getInstance(), nodeDisplay.entity());
      nodeDisplay.players().remove(player);
    } else {
      nodeDisplay.entity().remove();
    }
  }

  private void renderNode(Player player, Node node) {
    if (rendered.containsKey(node.getNodeId())) {
      NodeDisplay nodeDisplay = rendered.get(node.getNodeId());
      BlockDisplay entity = nodeDisplay.entity();
      player.showEntity(PathFinderPlugin.getInstance(), entity);
      return;
    }
    BlockDisplay entity = createEntity(node);
    rendered.put(node.getNodeId(), new NodeDisplay(entity, Lists.newArrayList(player)));
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.getUniqueId().equals(player.getUniqueId())) {
        continue;
      }
      // p.hideEntity(PathFinderPlugin.getInstance(), entity);
    }
  }

  private static final double NODE_SCALE = .4;

  private BlockDisplay createEntity(Node node) {
    double h = NODE_SCALE / 2;
    Location l = BukkitVectorUtils.toBukkit(node.getLocation()).subtract(h, h, h);
    BlockDisplay blockDisplay = l.getWorld().spawn(l, BlockDisplay.class);
    blockDisplay.setBlock(Material.LIME_CONCRETE.createBlockData());
    blockDisplay.setInterpolationDelay(-1);
    Transformation t = blockDisplay.getTransformation();
    t.getScale().set(0);
    blockDisplay.setTransformation(t);
    blockDisplay.setInterpolationDuration(20);
    t.getScale().set(NODE_SCALE);
    blockDisplay.setTransformation(t);
    return blockDisplay;
  }
}
