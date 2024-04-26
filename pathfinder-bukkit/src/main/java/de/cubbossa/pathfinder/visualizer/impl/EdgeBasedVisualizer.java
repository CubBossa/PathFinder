package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public abstract class EdgeBasedVisualizer<ViewT extends EdgeBasedVisualizer<ViewT>.EdgeBasedView>
    extends IntervalVisualizer<ViewT> {

  /**
   * The amount of blocks that the lead point should move ahead of the player.
   * The lead point serves as target for visualizers like entities or compasses.
   */
  private double moveAhead = 5;

  public EdgeBasedVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public ViewT createView(UpdatingPath nodes, PathPlayer<Player> player) {

    List<Edge> edges = new ArrayList<>();
    Node prev = null;
    int index = 0;
    for (Node node : nodes) {
      if (prev == null) {
        prev = node;
        continue;
      }
      edges.add(new Edge(index++, BukkitVectorUtils.toBukkit(prev.getLocation()),
          BukkitVectorUtils.toBukkit(node.getLocation())));
      prev = node;
    }
    return createView(nodes, edges, player);
  }

  public List<Edge> getEdges() {

  }

  public abstract ViewT createView(UpdatingPath nodes, List<Edge> edges, PathPlayer<Player> player);

  @Getter
  public abstract class EdgeBasedView extends IntervalVisualizer<ViewT>.IntervalView {

    private final List<Edge> edges;
    private Location lastPlayerLocation;

    public EdgeBasedView(PathPlayer<Player> player, UpdatingPath nodes, List<Edge> edges) {
      super(player, nodes);
      this.edges = edges;
    }

    @Override
    void play(int interval) {
      if (getTargetViewer() == null) {
        return;
      }
      Player player = getTargetViewer().unwrap();
      if (player == null || !player.isOnline()) {
        return;
      }
      if (player.getLocation().equals(lastPlayerLocation)) {
        // No need to update, the player has not moved.
        return;
      }
      lastPlayerLocation = player.getLocation();

      // find nearest edge
      Edge nearest = null;
      double edgeNearestDist = Double.MAX_VALUE;
      for (Edge edge : edges) {
        double dist = BukkitVectorUtils.distancePointToSegment(
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
      Vector closestPoint = BukkitVectorUtils.closestPointOnSegment(
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
        currentEdge = currentEdge.index() + 1 >= edges.size() ? null : edges.get(currentEdge.index() + 1);
      }

      // do whatever you want with the retrieved data. Example in CompassVisualizer
      play(closestPoint.toLocation(Objects.requireNonNull(currentPoint.getWorld())), currentPoint, nearest);
    }

    public abstract void play(Location nearestPoint, Location leadPoint, Edge nearestEdge);
  }


  protected record Edge(int index, Location support, Location target) {

  }
}
