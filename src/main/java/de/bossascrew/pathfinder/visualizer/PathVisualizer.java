package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.util.NodeUtils;
import de.bossascrew.splinelib.interpolate.Interpolation;
import de.bossascrew.splinelib.util.Spline;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public interface PathVisualizer extends Keyed, Named {

	@Nullable String getPermission();

	double getTangentLength();

	int getTickDelay();

	default Spline makeSpline(List<Node> nodes) {
		double tl = getTangentLength();
		return tl == 0 ?
				nodes.stream().map(Node::getPosition).map(PathPlugin.SPLINES::convertToBezierVector).collect(Collectors.toCollection(Spline::new)) :
				new Spline(NodeUtils.toSpline(nodes, tl));
	}

	default List<Vector> interpolate(Spline bezierVectors) {
		return PathPlugin.SPLINES.newCurveBuilder(bezierVectors)
				.withClosedPath(false)
				.withRoundingInterpolation(Interpolation.linearInterpolation(.2))
				.withSpacingInterpolation(Interpolation.equidistantInterpolation(.2))
				.buildAndConvert();
	}

	default List<Vector> transform(List<Vector> curve) {
		return curve;
	}

	void playParticle(Player player, Location location, int index, long time);
}
