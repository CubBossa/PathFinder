package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.core.node.Node;
import de.bossascrew.splinelib.util.BezierVector;
import de.cubbossa.pathfinder.PathPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class NodeUtils {

	public static List<BezierVector> toSpline(LinkedHashMap<Node, Double> path) {
		if (path.size() <= 1) {
			throw new IllegalArgumentException("Path to modify must have at least two points.");
		}
		Node[] nodes = path.keySet().toArray(new Node[0]);
		List<BezierVector> vectors = new ArrayList<>();

		Node startNode = nodes[0];
		Vector start = startNode.getPosition();
		Vector next = nodes[1].getPosition();
		Vector startDirFull = next.clone().subtract(start);
		double startDirFullLength = startDirFull.length();
		double tangentLength = path.get(startNode);
		if (tangentLength > startDirFullLength / 3) {
			tangentLength = startDirFullLength / 3;
		}
		Vector startDir = startDirFull.normalize().multiply(tangentLength);
		vectors.add(new BezierVector(PathPlugin.SPLINES.convertToVector(start), PathPlugin.SPLINES.convertToVector(start.clone().add(startDir)), PathPlugin.SPLINES.convertToVector(start.clone().add(startDir.multiply(-1)))));

		for (int i = 1; i < path.size() - 1; i++) {
			Node node = nodes[i];
			Vector p = nodes[i - 1].getPosition();
			Vector c = node.getPosition();
			Vector n = nodes[i + 1].getPosition();
			Vector u = p.clone().crossProduct(n).normalize();
			Vector r = p.clone().add(n.clone().subtract(p).multiply(.5f)).normalize();

			Vector dir = u.clone().crossProduct(r).normalize();
			double tl = path.get(node);

			vectors.add(new BezierVector(PathPlugin.SPLINES.convertToVector(c),
					PathPlugin.SPLINES.convertToVector(c.clone().add(dir.clone().multiply(-1 * tl))),
					PathPlugin.SPLINES.convertToVector(c.clone().add(dir.multiply(tl)))));
		}

		Node endNode = nodes[nodes.length - 1];
		Vector end = endNode.getPosition();
		Vector prev = nodes[nodes.length - 2].getPosition();
		Vector endDir = end.clone().subtract(prev).normalize().multiply(path.get(endNode));
		vectors.add(new BezierVector(PathPlugin.SPLINES.convertToVector(end), PathPlugin.SPLINES.convertToVector(end.clone().add(endDir).multiply(-1)), PathPlugin.SPLINES.convertToVector(prev.clone().add(endDir))));

		return vectors;
	}

}
