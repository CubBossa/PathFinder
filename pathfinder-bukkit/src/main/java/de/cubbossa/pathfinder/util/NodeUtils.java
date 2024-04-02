package de.cubbossa.pathfinder.util;

import com.google.common.base.Preconditions;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.splinelib.util.BezierVector;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NodeUtils {

  /**
   * Lists all waypoints of a certain selection.
   */
  public static void onList(Player player, NodeSelection selection, Pagination pagination) {

    String selector;
    if (selection.getMeta() != null) {
      selector = selection.getMeta().selector();
    } else {
      selector = "@n";
    }

    BukkitUtils.wrap(player).sendMessage(Messages.CMD_N_LIST
        .insertList("nodes", selection)
        .insertString("selector", selector));
  }

  public static List<BezierVector> toSpline(LinkedHashMap<Node, Double> path, boolean preventLoopsFromHighWeights) {
    Preconditions.checkState(path.size() > 0);

    if (path.size() < 2) {
      return List.of(new BezierVector(AbstractPathFinder.SPLINES.convertToVector(path.keySet().iterator().next().getLocation()), null, null));
    }

    BezierVector[] vectors = new BezierVector[path.size()];
    de.cubbossa.splinelib.util.Vector[] dirs = new de.cubbossa.splinelib.util.Vector[path.size()];
    double[] leftWeights = new double[path.size()];
    double[] rightWeights = new double[path.size()];

    int index = 0;
    for (Map.Entry<Node, Double> entry : path.entrySet()) {
      vectors[index] = new BezierVector(AbstractPathFinder.SPLINES.convertToVector(entry.getKey().getLocation()), null, null);
      dirs[index] = null;
      leftWeights[index] = entry.getValue();
      rightWeights[index++] = entry.getValue();
    }

    @Nullable BezierVector previous;
    BezierVector current;
    @Nullable BezierVector next;

    for (int i = 0; i < path.size(); i++) {
      current = vectors[i];
      next = i == path.size() - 1 ? (BezierVector) current.clone() : vectors[i + 1];

      if (i == 0) {
        dirs[i] = next.toVector().subtract(current).normalize();
        continue;
      }
      previous = i < path.size() - 1
          ? vectors[i - 1]
          : (BezierVector) current.clone();

      de.cubbossa.splinelib.util.Vector a = next.toVector().subtract(current);
      de.cubbossa.splinelib.util.Vector b = current.toVector().subtract(previous);
      boolean anull = a.lengthSquared() == 0;
      boolean bnull = b.lengthSquared() == 0;
      if (!anull) a = a.normalize();
      if (!bnull) b = b.normalize();
      de.cubbossa.splinelib.util.Vector dir = null;
      if (!anull || !bnull) {
        de.cubbossa.splinelib.util.Vector sum = a.add(b);
        dir = sum.lengthSquared() == 0 ? null : sum.normalize();
      }
      dirs[i] = dir;

      if (i == path.size() - 1) {
        continue;
      }

      // Set weights for previous and current -> if they have more than dist weight we need to proportionally share.
      double rightWeight = rightWeights[i - 1], leftWeight = leftWeights[i];

      if (preventLoopsFromHighWeights) {
        // distance times 0.8 so the neighbouring controllers don't touch each other
        double dist = previous.distance(current) * .8;
        // They would touch each other -> shorten them
        if (rightWeight + leftWeight > dist) {
          double r = rightWeight / (rightWeight + leftWeight);
          rightWeight = r * dist;
          leftWeight = (1 - r) * dist;
          rightWeights[i - 1] = rightWeight;
          leftWeights[i] = leftWeight;
        }
      }
      if (dirs[i - 1] != null) {
        previous.setRightControlPoint(previous.toVector().add(dirs[i - 1].clone().multiply(rightWeight)));
      }
      if (dir != null) {
        current.setLeftControlPoint(current.toVector().add(dir.clone().multiply(-1).multiply(leftWeight)));
      }
    }
    return List.of(vectors);
  }
}
