package de.bossascrew.pathfinder.old.system;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

public class PathCornerCalculator {
	
	public Vector tangentPrevious;
	public Vector tangentLeft;
	public Vector corner;
	public Vector tangentRight;
	
	public PathCornerCalculator(Node previous, Node corner, Node after, Vector tangentPrevious) {

		if(corner == null) {
			System.out.println("Fehlerhafte PathCorner initialisierung! Corner muss angegeben sein");
			return;
		}
		this.corner = corner.loc;
		tangentLeft = previous == null ? null : getTangent(corner.loc, previous.loc, corner.tangentReach, previous.tangentReach);
		tangentRight = after == null ? null : getTangent(corner.loc, after.loc, corner.tangentReach, after.tangentReach);	
		this.tangentPrevious = tangentPrevious;
	}
	
	private Vector getTangent(Vector a, Vector b, double aSmoothing, double bSmoothing) {
		if(a == null || b == null) return null;
		
		double distAB = a.distance(b);
		double dist = distAB < aSmoothing+bSmoothing ? distAB * (aSmoothing / (aSmoothing+bSmoothing)) : aSmoothing;
		
		Vector tangentPoint = a.clone().add(b.clone().subtract(a).normalize().multiply(dist));
		return tangentPoint;
	}
	
	public List<Vector> getPointsByDistance(double distance) {
		List<Vector> ret = new ArrayList<Vector>();
		
		if(tangentPrevious == null || tangentLeft == null) return ret;
		//LINEAR DISPLAY
		double lengthLinear = (tangentPrevious.clone().subtract(tangentLeft)).length() - distance;
		for(double t1 = 0; t1 < lengthLinear-distance; t1 += distance) {
			Vector v = tangentPrevious.clone().multiply((1-(t1/lengthLinear))).add(tangentLeft.clone().multiply(t1/lengthLinear));
			ret.add(v);
		}
		//ROUND DISPLAY
		if(tangentRight == null) return ret;
		double lengthBezier = (corner.clone().subtract(tangentLeft)).length() + (tangentRight.clone().subtract(corner)).length() - distance;
		for(double t2 = 0; t2 < lengthBezier; t2 += distance) {
			Vector v = tangentLeft.clone().multiply(Math.pow(1 - (t2/lengthBezier), 2)).add(corner.clone().multiply(2 * (1 - (t2/lengthBezier)) * (t2/lengthBezier)).add(tangentRight.clone().multiply(Math.pow((t2/lengthBezier), 2))));
			ret.add(v);
		}
		return ret;
	}
}
