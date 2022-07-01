package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.splinelib.util.BezierVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static de.bossascrew.pathfinder.PathPlugin.SPLINES;

public class NodeUtils {

	public static List<BezierVector> toSpline(List<Node> path, double tangentLength) {
		if (path.size() <= 1) {
			throw new IllegalArgumentException("Path to modify must have at least two points.");
		}
		List<BezierVector> vectors = new ArrayList<>();

		Vector start = path.get(0).getPosition();
		Vector next = path.get(1).getPosition();
		Vector startDir = next.clone().subtract(start).normalize().multiply(tangentLength);
		vectors.add(new BezierVector(SPLINES.convertToVector(start), SPLINES.convertToVector(start.clone().add(startDir)), SPLINES.convertToVector(start.clone().add(startDir.multiply(-1)))));

		for (int i = 1; i < path.size() - 1; i++) {
			Vector p = path.get(i - 1).getPosition();
			Vector c = path.get(i).getPosition();
			Vector n = path.get(i + 1).getPosition();
			Vector u = p.clone().crossProduct(n).normalize();
			Vector r = p.clone().add(n.clone().subtract(p).multiply(.5f)).normalize();

			Vector dir = u.clone().crossProduct(r).normalize().multiply(tangentLength);
			vectors.add(new BezierVector(SPLINES.convertToVector(c), SPLINES.convertToVector(c.clone().add(dir.clone().multiply(-1))), SPLINES.convertToVector(c.clone().add(dir))));
		}

		Vector end = path.get(path.size() - 1).getPosition();
		Vector prev = path.get(path.size() - 2).getPosition();
		Vector endDir = end.clone().subtract(prev).normalize().multiply(tangentLength);
		vectors.add(new BezierVector(SPLINES.convertToVector(prev), SPLINES.convertToVector(end.clone().add(endDir)), SPLINES.convertToVector(prev.clone().add(endDir).multiply(-1))));

		return vectors;
	}

}
