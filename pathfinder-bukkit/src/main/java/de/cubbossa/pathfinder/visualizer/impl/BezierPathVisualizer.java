package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.group.CurveLengthModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.util.NodeUtils;
import de.cubbossa.splinelib.interpolate.Interpolation;
import de.cubbossa.splinelib.util.Spline;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class BezierPathVisualizer
    extends IntervalVisualizer<BezierPathVisualizer.BezierView> {

  private float pointDistance = .2f;
  private int bezierSamplingRate = 16;

  public BezierPathVisualizer(NamespacedKey key) {
    super(key);
  }

  /**
   * Converts a path of nodes to a spline object, which, again, can be converted into a list of locations that form a curved path.
   *
   * @param nodes A map of nodes with a curve length for each node.
   * @return a spline object representing the nodes
   */
  private Spline makeSpline(LinkedHashMap<Node, Double> nodes) {
    return new Spline(NodeUtils.toSpline(nodes, true));
  }

  private List<Vector> interpolate(Spline bezierVectors) {
    return CommonPathFinder.SPLINES.newCurveBuilder(bezierVectors)
        .withClosedPath(false)
        .withRoundingInterpolation(Interpolation.bezierInterpolation(bezierSamplingRate))
        .withSpacingInterpolation(Interpolation.equidistantInterpolation(pointDistance))
        .buildAndConvert().stream()
        .map(vector -> new Vector(vector.getX(), vector.getY(), vector.getZ()))
        .collect(Collectors.toList());
  }

  private List<Vector> transform(List<Vector> curve) {
    return curve;
  }

  @Getter
  public abstract class BezierView extends IntervalVisualizer<BezierView>.IntervalView {
    List<Location> points;

    public BezierView(PathPlayer<Player> player, List<Location> points) {
      super(player);
      this.points = points;
    }

    public BezierView(PathPlayer<Player> player, Node... nodes) {
      super(player);

      // split the path into segments for each appearing world change
      List<PathSegment> segments = new ArrayList<>();
      World last = null;
      List<Node> open = new ArrayList<>();
      for (Node node : nodes) {
        if (!Objects.equals(last, node.getLocation().getWorld())) {
          if (last != null) {
            segments.add(new PathSegment(last, new ArrayList<>(open)));
            open.clear();
          }
          last = node.getLocation().getWorld();
        }
        open.add(node);
      }
      if (!open.isEmpty()) {
        segments.add(new PathSegment(last, new ArrayList<>(open)));
      }

      // make a smooth spline for each segment and append them.
      List<Location> calculatedPoints = new ArrayList<>();
      for (PathSegment segment : segments) {
        LinkedHashMap<Node, Double> path = new LinkedHashMap<>();
        for (Node node : segment.nodes()) {
          CurveLengthModifier mod = StorageUtil.getGroups(node).stream()
              .filter(g -> g.hasModifier(CurveLengthModifier.class))
              .sorted()
              .map(g -> g.<CurveLengthModifier>getModifier(CurveLengthModifier.KEY))
              .filter(Optional::isPresent).map(Optional::get)
              .findFirst().orElse(null);

          path.put(node, mod == null ? 1 : mod.curveLength());
        }
        Spline spline = makeSpline(path);
        List<Vector> curve = transform(interpolate(spline));
        org.bukkit.World world = Bukkit.getWorld(segment.world().getUniqueId());
        calculatedPoints.addAll(curve.stream().map(vector -> vector.toLocation(world)).toList());
      }

      points = new ArrayList<>(calculatedPoints);
    }
  }

  private record PathSegment(World world, List<Node> nodes) {
  }
}
