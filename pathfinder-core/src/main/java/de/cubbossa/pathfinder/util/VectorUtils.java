package de.cubbossa.pathfinder.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {

  public static final Vector NORTH = new Vector(0, 0, -1);
  public static final Vector EAST = new Vector(1, 0, 0);
  public static final Vector SOUTH = new Vector(0, 0, 1);
  public static final Vector WEST = new Vector(-1, 0, 0);

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

  public static double convertDirectionToXZAngle(Location location) {
    return convertDirectionToXZAngle(location.getDirection());
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

  public static double convertYawToAngle(Location location) {
    return convertYawToAngle(location.getYaw());
  }

  public static double convertYawToAngle(double yaw) {
    return (yaw + 180) % 360;
  }

  public static Location toBukkit(de.cubbossa.pathfinder.api.misc.Location internal) {
    return new Location(Bukkit.getWorld(internal.getWorld().getUniqueId()), internal.getX(), internal.getY(), internal.getZ());
  }

  public static Vector toBukkit(de.cubbossa.pathfinder.api.misc.Vector internal) {
    return new Vector(internal.getX(), internal.getY(), internal.getZ());
  }

  public static de.cubbossa.pathfinder.api.misc.Location toInternal(Location bukkit) {
    return new de.cubbossa.pathfinder.api.misc.Location(bukkit.getX(), bukkit.getY(), bukkit.getZ(), new WorldImpl(bukkit.getWorld().getUID()));
  }

  public static de.cubbossa.pathfinder.api.misc.Vector toInternal(Vector bukkit) {
    return new de.cubbossa.pathfinder.api.misc.Vector(bukkit.getX(), bukkit.getY(), bukkit.getZ());
  }
}
