package de.cubbossa.pathfinder.editmode.renderer;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.FutureUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EdgeEntityRenderer extends AbstractEntityRenderer<Edge, BlockDisplay>
    implements GraphRenderer<Player> {

  private static final Vector3f NODE_SCALE = new Vector3f(1f, 1f, 1.618f).mul(0.25f);

  public static final Action<TargetContext<Edge>> RIGHT_CLICK_EDGE = new Action<>();
  public static final Action<TargetContext<Edge>> LEFT_CLICK_EDGE = new Action<>();

  public EdgeEntityRenderer(JavaPlugin plugin) {
    super(plugin, BlockDisplay.class);
    setRenderDistance(PathFinderProvider.get().getConfiguration().getEditMode().getEdgeArmorStandRenderDistance());
  }

  @Override
  CompletableFuture<Location> location(Edge element) {
    return FutureUtils
        .both(
            element.resolveStart().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit),
            element.resolveEnd().thenApply(Node::getLocation).thenApply(BukkitVectorUtils::toBukkit)
        )
        .thenApply(e -> BukkitUtils.lerp(e.getKey(), e.getValue(), .3d));
  }

  Action<TargetContext<Edge>> handleInteract(Player player, int slot, boolean left) {
    return left ? LEFT_CLICK_EDGE : RIGHT_CLICK_EDGE;
  }

  @Override
  boolean equals(Edge a, Edge b) {
    return a.getStart().equals(b.getStart()) && a.getEnd().equals(b.getEnd());
  }

  @Override
  void render(Edge element, BlockDisplay entity) {
    Vector dir = element.resolveStart().thenCompose(start -> {
      return element.resolveEnd().thenApply(end -> {
        return BukkitVectorUtils.toBukkit(end.getLocation().clone().subtract(start.getLocation()).asVector());
      });
    }).join();
    entity.setBlock(Material.ORANGE_CONCRETE.createBlockData());

    entity.setTransformationMatrix(new Matrix4f()
        .rotateTowards(new Vector3f((float) dir.getX(), (float) dir.getY(), (float) dir.getZ()), new Vector3f(0, 1, 0))
        .translate(new Vector3f(.5f).mul(NODE_SCALE).mul(-1))
        .scale(NODE_SCALE)
    );
  }

  @Override
  public void showElement(Edge element, Player player) {
    super.showElement(element, player);

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
      // all edges from rendered nodes to adjacent nodes
      Collection<Edge> toRender = nodes.stream()
          .map(Node::getEdges).flatMap(Collection::stream)
          .collect(Collectors.toSet());

      Collection<UUID> ids = nodes.stream().map(Node::getNodeId).toList();
      hideElements(getNodeEntityMap().keySet().stream().filter(edge -> ids.contains(edge.getStart())).toList(), player.unwrap());

      showElements(toRender, player.unwrap());
      players.add(player);
    }, BukkitPathFinder.mainThreadExecutor());
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.runAsync(() -> {
      Collection<UUID> nodeIds = nodes.stream().map(Node::getNodeId).collect(Collectors.toSet());
      Collection<Edge> toErase = nodeEntityMap.keySet().stream()
          .filter(edge -> nodeIds.contains(edge.getStart()) || nodeIds.contains(edge.getEnd()))
          .collect(Collectors.toSet());
      hideElements(toErase, player.unwrap());
    }, BukkitPathFinder.mainThreadExecutor());
  }
}
