package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.editmode.utils.ItemStackUtils;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.FutureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EdgeArmorStandRenderer extends AbstractArmorstandRenderer<Edge>
    implements GraphRenderer<Player> {

  public static final Action<TargetContext<Edge>> RIGHT_CLICK_EDGE = new Action<>();
  public static final Action<TargetContext<Edge>> LEFT_CLICK_EDGE = new Action<>();
  private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -.9, 0);

  private final ItemStack nodeHead =
      ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_ORANGE);

  public EdgeArmorStandRenderer(JavaPlugin plugin) {
    super(plugin);
  }

  @Override
  Location retrieveFrom(Edge element) {
    return FutureUtils
        .both(
            element.resolveStart().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit),
            element.resolveEnd().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit)
        )
        .thenApply(e -> BukkitUtils.lerp(e.getKey(), e.getValue(), .3d).add(ARMORSTAND_CHILD_OFFSET))
        .join();
  }

  @Override
  Action<TargetContext<Edge>> handleInteract(Player player, int slot, boolean left) {
    return left ? LEFT_CLICK_EDGE : RIGHT_CLICK_EDGE;
  }

  @Override
  boolean equals(Edge a, Edge b) {
    return a.getStart().equals(b.getStart()) && a.getEnd().equals(b.getEnd());
  }

  @Override
  ItemStack head(Edge element) {
    return nodeHead.clone();
  }

  @Override
  public void showElement(Edge element, Player player) {
    super.showElement(element, player);
    setHeadRotation(player, nodeEntityMap.get(element), element.resolveStart().thenApply(start -> {
      Node end = element.resolveEnd().join();
      return BukkitVectorUtils.toBukkit(end.getLocation().clone().subtract(start.getLocation()).asVector());
    }).join());
  }

  @Override
  boolean isSmall(Edge element) {
    return true;
  }

  @Nullable
  @Override
  Component getName(Edge element) {
    return null;
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    hideElements(entityNodeMap.values(), player.unwrap());
    players.remove(player);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {

    // all edges from rendered nodes to adjacent nodes
    Collection<Edge> toRender = nodes.stream()
        .map(Node::getEdges).flatMap(Collection::stream)
        .collect(Collectors.toSet());

    showElements(toRender, player.unwrap());
    players.add(player);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    hideElements(nodes.stream().flatMap(n -> n.getEdges().stream()).toList(), player.unwrap());
    return CompletableFuture.completedFuture(null);
  }
}
