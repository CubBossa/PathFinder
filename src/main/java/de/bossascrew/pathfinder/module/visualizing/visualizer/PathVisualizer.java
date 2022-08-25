package de.bossascrew.pathfinder.module.visualizing.visualizer;

import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.util.NodeUtils;
import de.bossascrew.splinelib.interpolate.Interpolation;
import de.bossascrew.splinelib.util.Spline;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface PathVisualizer extends Keyed, Named {

	@Nullable
	String getPermission();

	int getTickDelay();

	default float getEquidistance() {
		return .2f;
	}

	default int getBezierSamplingRate() {
		return 16;
	}

	/**
	 * Converts a path of nodes to a spline object, which, again, can be converted into a list of locations that form a curved path.
	 *
	 * @param nodes A map of nodes with a curve length for each node.
	 * @return a spline object representing the nodes
	 */
	default Spline makeSpline(LinkedHashMap<Node, Double> nodes) {
		return new Spline(NodeUtils.toSpline(nodes));
	}

	default List<Vector> interpolate(Spline bezierVectors) {
		return PathPlugin.SPLINES.newCurveBuilder(bezierVectors)
				.withClosedPath(false)
				.withRoundingInterpolation(Interpolation.bezierInterpolation(getBezierSamplingRate()))
				.withSpacingInterpolation(Interpolation.equidistantInterpolation(getEquidistance()))
				.buildAndConvert();
	}

	default List<Vector> transform(List<Vector> curve) {
		return curve;
	}

	void play(List<Location> path, VisualizerContext context);

	record VisualizerContext(List<Player> players, int interval, long time) {

		public Player player() {
			return players.get(0);
		}
	}
}
