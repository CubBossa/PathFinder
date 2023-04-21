package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class EdgeBasedVisualizer<T extends PathVisualizer<T, D, Player>, D extends EdgeBasedVisualizer.Data>
		extends BukkitVisualizer<T, D> {

  private int interval = 10;
  private @Nullable String permission;
  /**
   * The amount of blocks that the lead point should move ahead of the player.
   * The lead point serves as target for visualizers like entities or compasses.
   */
  private double moveAhead = 5;

  public EdgeBasedVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public D prepare(List<Node<?>> nodes, PathPlayer<Player> player) {

    List<Edge> edges = new ArrayList<>();
    Node<?> prev = null;
    int index = 0;
    for (Node<?> node : nodes) {
      if (prev == null) {
        prev = node;
        continue;
      }
      edges.add(new Edge(index++, VectorUtils.toBukkit(prev.getLocation()), VectorUtils.toBukkit(node.getLocation())));
      prev = node;
    }
    return newData(player, nodes, edges);
  }

  public abstract D newData(PathPlayer<Player> player, List<Node<?>> nodes, List<Edge> edges);

  @Override
  public void play(PathVisualizer.VisualizerContext<D, Player> context) {
    PathPlayer<Player> targetPlayer = context.player();
    Player player = targetPlayer.unwrap();

    // No need to update, the player has not moved.
    if (player.getLocation().equals(context.data().getLastPlayerLocation())) {
      return;
    }
    context.data().setLastPlayerLocation(player.getLocation());

    // find nearest edge
    Edge nearest = null;
    double edgeNearestDist = Double.MAX_VALUE;
    for (Edge edge : context.data().getEdges()) {
      double dist = VectorUtils.distancePointToSegment(
          player.getEyeLocation().toVector(),
          edge.support().toVector(),
          edge.target().toVector());
      if (dist < edgeNearestDist) {
        nearest = edge;
        edgeNearestDist = dist;
      }
    }


    if (nearest == null) {
      throw new RuntimeException("The path does not contain any edges.");
    }

    // find the closest point on closest edge and move some blocks along in direction of target.
    Vector closestPoint = VectorUtils.closestPointOnSegment(
        player.getEyeLocation().toVector(),
        nearest.support().toVector(),
        nearest.target().toVector()
    );

    // shift the closest point 5 units towards final target location
    double unitsToShift = moveAhead;
    Location currentPoint = closestPoint.toLocation(player.getWorld());
    Edge currentEdge = nearest;
    while (currentEdge != null && unitsToShift > 0) {
      double dist = currentPoint.distance(currentEdge.target());
      if (dist > unitsToShift) {
        currentPoint.add(
            currentEdge.target().clone().subtract(currentEdge.support()).toVector().normalize()
                .multiply(unitsToShift));
        break;
      }
      unitsToShift -= dist;
      currentPoint = currentEdge.target().clone();
      currentEdge = currentEdge.index() + 1 >= context.data().getEdges().size()
          ? null
          : context.data().getEdges().get(currentEdge.index() + 1);
    }

    // do whatever you want with the retrieved data. Example in CompassVisualizer
    play(context, closestPoint.toLocation(Objects.requireNonNull(currentPoint.getWorld())),
        currentPoint, nearest);
  }

  public abstract void play(PathVisualizer.VisualizerContext<D, Player> context, Location nearestPoint, Location leadPoint,
                            Edge nearestEdge);

  protected record Edge(int index, Location support, Location target) {
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  public static class Data {
    private final List<Node<?>> nodes;
    private final List<Edge> edges;
    private Location lastPlayerLocation;
  }
}