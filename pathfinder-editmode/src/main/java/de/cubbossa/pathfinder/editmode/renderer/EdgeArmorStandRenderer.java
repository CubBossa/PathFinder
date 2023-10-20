package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.PathFinderProvider;
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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.LEFT_CLICK_EDGE;
import static de.cubbossa.pathfinder.editmode.menu.EditModeMenu.RIGHT_CLICK_EDGE;

public class EdgeArmorStandRenderer extends AbstractArmorstandRenderer<Edge>
    implements GraphRenderer<Player> {

  private static final Vector ARMORSTAND_CHILD_OFFSET = new Vector(0, -.9, 0);

  private final ItemStack nodeHead = ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_ORANGE);

  public EdgeArmorStandRenderer(JavaPlugin plugin) {
    super(plugin);
    setRenderDistance(PathFinderProvider.get().getConfiguration().getEditMode().getEdgeArmorStandRenderDistance());
  }

  @Override
  CompletableFuture<Location> retrieveFrom(Edge element) {
    return FutureUtils
        .both(
            element.resolveStart().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit),
            element.resolveEnd().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit)
        )
        .thenApply(e -> BukkitUtils.lerp(e.getKey(), e.getValue(), .3d).add(ARMORSTAND_CHILD_OFFSET));
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
    element.resolveStart().thenCompose(start -> {
      return element.resolveEnd().thenApply(end -> {
        return BukkitVectorUtils.toBukkit(end.getLocation().clone().subtract(start.getLocation()).asVector());
      });
    }).thenAccept(vector -> {
      Location location = new Location(null, 0, 0, 0);
      location.setDirection(vector);
      ArmorStand e = entityNodeMap.inverse().get(element);
      e.setHeadPose(new EulerAngle(location.getPitch(), location.getYaw(), 0));
        ps(player).announce();
    });
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
    return CompletableFuture.runAsync(() -> {
      hideElements(entityNodeMap.values(), player.unwrap());
      players.remove(player);
    });
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      // all edges from rendered nodes to adjacent nodes
      Collection<Edge> toRender = nodes.stream()
          .map(Node::getEdges).flatMap(Collection::stream)
          .collect(Collectors.toSet());

      Collection<UUID> ids = nodes.stream().map(Node::getNodeId).toList();
      hideElements(entityNodeMap.values().stream().filter(edge -> ids.contains(edge.getStart())).toList(), player.unwrap());

      showElements(toRender, player.unwrap());
      players.add(player);
    });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {

    Collection<UUID> nodeIds = nodes.stream().map(Node::getNodeId).collect(Collectors.toSet());
    Collection<Edge> toErase = entityNodeMap.values().stream()
        .filter(edge -> nodeIds.contains(edge.getStart()) || nodeIds.contains(edge.getEnd()))
        .collect(Collectors.toSet());
    hideElements(toErase, player.unwrap());
    return CompletableFuture.completedFuture(null);
  }
}
