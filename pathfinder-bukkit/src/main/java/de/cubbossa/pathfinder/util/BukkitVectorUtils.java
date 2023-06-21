package de.cubbossa.pathfinder.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BukkitVectorUtils {

  public static final Vector UP = new Vector(0, 1, 0);
  public static final Vector DOWN = new Vector(0, -1, 0);
  public static final Vector NORTH = new Vector(0, 0, -1);
  public static final Vector EAST = new Vector(1, 0, 0);
  public static final Vector SOUTH = new Vector(0, 0, 1);
  public static final Vector WEST = new Vector(-1, 0, 0);

  public record Orientation(Vector location, Vector direction) {
  }

  public static Orientation getIntersection(Vector linePoint, Vector lineDirection, Vector block) {
    // First check the three directions of the line and by that sort out the three block faces
    // if the line is orthogonal to a block face, the value is null
    Vector[] blockFaces = {
        // put up first bc most likely player will be clicking from top. Will reduce by one iteration
        lineDirection.getY() < 0 ? UP : lineDirection.getY() > 0 ? DOWN : null,
        lineDirection.getX() < 0 ? EAST : lineDirection.getX() > 0 ? WEST : null,
        lineDirection.getZ() < 0 ? SOUTH : lineDirection.getZ() > 0 ? NORTH : null
    };
    Vector blocKEnd = block.clone().add(new Vector(1, 1, 1));
    for (Vector blockFace : blockFaces) {
      if (blockFace == null) {
        continue;
      }
      Vector faceShift = new Vector(1, 1, 1).multiply(.5).multiply(blockFace.clone().multiply(blockFace))
          .add(blockFace.clone().multiply(.5));
      Vector intersect = getIntersection(linePoint, lineDirection, block.clone().add(faceShift), blockFace);
      if (intersect == null) {
        continue;
      }
      // check if intersection is in 1x1 block face of plane
      if (intersect.isInAABB(block, blocKEnd)) {
        return new Orientation(intersect, blockFace);
      }
    }
    return null;
  }

  public static double collapse(Vector vector) {
    return vector.getX() + vector.getY() + vector.getZ();
  }

  public static Vector getIntersection(Vector linePoint, Vector lineDirection, Vector planeOrigin, Vector planeNormal) {
    double epsilon = Math.pow(10, -6);

    double dot = planeNormal.dot(lineDirection);
    if (Math.abs(dot) < epsilon) {
      return null;
    }
    Vector dir = linePoint.clone().subtract(planeOrigin);
    double si = -planeNormal.dot(dir) / dot;
    return planeOrigin.clone().add(dir).add(lineDirection.clone().multiply(si));
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

  public static de.cubbossa.pathapi.misc.Vector closestPointOnSegment(de.cubbossa.pathapi.misc.Vector point, de.cubbossa.pathapi.misc.Vector lineSupport, de.cubbossa.pathapi.misc.Vector lineTarget) {
    return toInternal(closestPointOnSegment(toBukkit(point), toBukkit(lineSupport), toBukkit(lineTarget)));
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
    return convertDirectionToXZAngle(location.toVector());
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

  public static Location toBukkit(de.cubbossa.pathapi.misc.Location internal) {
    return new Location(Bukkit.getWorld(internal.getWorld().getUniqueId()), internal.getX(),
        internal.getY(), internal.getZ());
  }

  public static Vector toBukkit(de.cubbossa.pathapi.misc.Vector internal) {
    return new Vector(internal.getX(), internal.getY(), internal.getZ());
  }

  public static de.cubbossa.pathapi.misc.Location toInternal(Location bukkit) {
    return new de.cubbossa.pathapi.misc.Location(bukkit.getX(), bukkit.getY(), bukkit.getZ(),
        new WorldImpl(bukkit.getWorld().getUID()));
  }

  public static de.cubbossa.pathapi.misc.Vector toInternal(Vector bukkit) {
    return new de.cubbossa.pathapi.misc.Vector(bukkit.getX(), bukkit.getY(), bukkit.getZ());
  }
}
