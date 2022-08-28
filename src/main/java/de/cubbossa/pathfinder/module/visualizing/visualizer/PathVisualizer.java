package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.bossascrew.splinelib.interpolate.Interpolation;
import de.bossascrew.splinelib.util.Spline;
import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PermissionHolder;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.NodeUtils;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;

public interface PathVisualizer<T extends PathVisualizer<T>> extends Keyed, Named, PermissionHolder {

	int getInterval();

	void setInterval(int interval);

	VisualizerType<T> getType();

	float getPointDistance();

	void setPointDistance(float distance);

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
				.withSpacingInterpolation(Interpolation.equidistantInterpolation(getPointDistance()))
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

	void setSchedulerSteps(int steps);

	int getSchedulerSteps();
}
