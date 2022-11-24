package de.cubbossa.pathfinder.util;

import org.bukkit.util.Vector;

public class VectorUtils {

	public static double distancePointToLine(Vector point, Vector lineSupport, Vector lineTarget) {
		Vector a = point.clone();
		Vector b = lineSupport.clone();
		Vector c = lineTarget.clone();
		return a.clone().subtract(b).crossProduct(a.subtract(c)).length() / c.subtract(b).length();
	}

}
