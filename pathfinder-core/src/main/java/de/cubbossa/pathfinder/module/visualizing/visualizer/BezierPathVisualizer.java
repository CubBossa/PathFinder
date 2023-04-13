package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.node.Groupable;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.modifier.CurveLengthModifier;
import de.cubbossa.pathfinder.util.NodeUtils;
import de.cubbossa.splinelib.interpolate.Interpolation;
import de.cubbossa.splinelib.util.Spline;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public abstract class BezierPathVisualizer<T extends BezierPathVisualizer<T>>
    extends Visualizer<T, BezierPathVisualizer.BezierData> {

  private float pointDistance = .2f;
  private int bezierSamplingRate = 16;

  public BezierPathVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public BezierData prepare(List<Node<?>> nodes, Player player) {

    //TODO has to be rewritten if portals are being introduced
    World world = nodes.get(0).getLocation().getWorld();
    LinkedHashMap<Node<?>, Double> path = new LinkedHashMap<>();
    for (Node<?> node : nodes) {
      if (!(node instanceof Groupable<?> groupable)) {
        path.put(node, 1.);
        continue;
      }
      CurveLengthModifier mod = groupable.getGroups().stream()
          .filter(g -> g.hasModifier(CurveLengthModifier.class))
          .sorted()
          .map(g -> g.getModifier(CurveLengthModifier.class))
          .findFirst().orElse(null);

      path.put(node, mod == null ? 1 : mod.curveLength());
    }
    Spline spline = makeSpline(path);
    List<Vector> curve = transform(interpolate(spline));
    List<Location> calculatedPoints =
        new ArrayList<>(curve.stream().map(vector -> vector.toLocation(world)).toList());
    return new BezierData(calculatedPoints);
  }

  /**
   * Converts a path of nodes to a spline object, which, again, can be converted into a list of locations that form a curved path.
   *
   * @param nodes A map of nodes with a curve length for each node.
   * @return a spline object representing the nodes
   */
  private Spline makeSpline(LinkedHashMap<Node<?>, Double> nodes) {
    return new Spline(NodeUtils.toSpline(nodes, true));
  }

  private List<Vector> interpolate(Spline bezierVectors) {
    return PathPlugin.SPLINES.newCurveBuilder(bezierVectors)
        .withClosedPath(false)
        .withRoundingInterpolation(Interpolation.bezierInterpolation(bezierSamplingRate))
        .withSpacingInterpolation(Interpolation.equidistantInterpolation(pointDistance))
        .buildAndConvert();
  }

  private List<Vector> transform(List<Vector> curve) {
    return curve;
  }

  public record BezierData(List<Location> points) {
  }
}
