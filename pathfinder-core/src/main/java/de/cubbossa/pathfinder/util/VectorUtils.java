package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Vector;

public class VectorUtils {

  public static final Vector NORTH = new Vector(0, 0, -1);
  public static final Vector EAST = new Vector(1, 0, 0);
  public static final Vector SOUTH = new Vector(0, 0, 1);
  public static final Vector WEST = new Vector(-1, 0, 0);

  public static Vector snap(Vector point, int segmentsPerUnit) {

    return snap(point, segmentsPerUnit, new Vector(0, 0, 0));
  }

  public static Vector snapCentered(Vector point, int segmentsPerUnit) {
    return snap(point, segmentsPerUnit, new Vector(1, 1, 1)
        .divide(segmentsPerUnit).divide(2));
  }

  public static Vector snap(Vector point, int segmentsPerUnit, Vector offset) {
    return new Vector(
        snapValue(point.getX() - offset.getX(), segmentsPerUnit) + offset.getX(),
        snapValue(point.getY() - offset.getY(), segmentsPerUnit) + offset.getY(),
        snapValue(point.getZ() - offset.getZ(), segmentsPerUnit) + offset.getZ()
    );
  }

  private static double snapValue(double value, int segmentsPerUnit) {
    return Math.round(value * segmentsPerUnit) / (double) segmentsPerUnit;
  }

  public static double distancePointToLine(Vector point, Vector lineSupport, Vector lineTarget) {
    Vector a = point.clone();
    Vector b = lineSupport.clone();
    Vector c = lineTarget.clone();
    return a.clone().subtract(b).crossProduct(a.subtract(c)).length() / c.subtract(b).length();
  }

  public static double distancePointToSegment(Vector point, Vector a, Vector b) {
    return closestPointOnSegment(point, a, b).distance(point);
  }

  public static Vector closestPointOnSegment(Vector point, Vector lineSupport, Vector lineTarget) {
    Vector dir = lineTarget.clone().subtract(lineSupport);
    Vector w = point.clone().subtract(lineSupport);
    double dotStart = w.dot(dir);
    if (dotStart <= 0) {
      return lineSupport.clone();
    }
    double dotEnd = dir.dot(dir);
    if (dotStart >= dotEnd) {
      return lineTarget.clone();
    }
    return lineSupport.clone().add(dir.multiply(dotStart / dotEnd));
  }

  public static double convertDirectionToXZAngle(Vector vector) {
    Vector v = vector.clone().multiply(new Vector(1, 0, 1));
    return (Math.toDegrees(clockwiseXZAngle(NORTH, v)) + 360) % 360;
  }

  public static double clockwiseXZAngle(Vector a, Vector b) {
    return Math.atan2(determinantXZ(a, b), a.dot(b));
  }

  public static double determinantXZ(Vector a, Vector b) {
    return a.getX() * b.getZ() - a.getZ() * b.getX();
  }

  public static double convertYawToAngle(double yaw) {
    return (yaw + 180) % 360;
  }
}
