package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.bossascrew.splinelib.interpolate.Interpolation;
import de.bossascrew.splinelib.util.Spline;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.NodeUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class BezierPathVisualizer<T extends BezierPathVisualizer<T>> extends Visualizer<T, BezierPathVisualizer.BezierData> {

	public record BezierData(List<Location> points) {
	}

	private float pointDistance = .2f;
	private int bezierSamplingRate = 16;

	public BezierPathVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public VisualizerType<T> getType() {
		return null;
	}

	@Override
	public BezierData prepare(List<Node> nodes) {

		//TODO has to be rewritten if portals are being introduced
		World world = nodes.get(0).getLocation().getWorld();
		Spline spline = makeSpline(nodes.stream().collect(Collectors.toMap(
				o -> o,
				o -> o.getCurveLength() == null ? RoadMapHandler.getInstance().getRoadMap(o.getRoadMapKey()).getDefaultBezierTangentLength() : o.getCurveLength(),
				(aDouble, aDouble2) -> aDouble,
				LinkedHashMap::new)));
		List<Vector> curve = transform(interpolate(spline));
		List<Location> calculatedPoints = new ArrayList<>(transform(curve).stream().map(vector -> vector.toLocation(world)).toList());
		return new BezierData(calculatedPoints);
	}

	/**
	 * Converts a path of nodes to a spline object, which, again, can be converted into a list of locations that form a curved path.
	 *
	 * @param nodes A map of nodes with a curve length for each node.
	 * @return a spline object representing the nodes
	 */
	private Spline makeSpline(LinkedHashMap<Node, Double> nodes) {
		return new Spline(NodeUtils.toSpline(nodes));
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
}
