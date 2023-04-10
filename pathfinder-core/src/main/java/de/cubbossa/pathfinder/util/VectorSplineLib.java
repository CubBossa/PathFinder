package de.cubbossa.pathfinder.util;

import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.splinelib.util.BezierVector;
import org.bukkit.util.Vector;

public class VectorSplineLib extends SplineLib<Vector> {
  @Override
  public de.cubbossa.splinelib.util.Vector convertToVector(org.bukkit.util.Vector vector) {
    return new de.cubbossa.splinelib.util.Vector(vector.getX(), vector.getY(), vector.getZ());
  }

  @Override
  public org.bukkit.util.Vector convertFromVector(de.cubbossa.splinelib.util.Vector vector) {
    return new Vector(vector.getX(), vector.getY(), vector.getZ());
  }

  @Override
  public BezierVector convertToBezierVector(org.bukkit.util.Vector vector) {
    return new BezierVector(vector.getX(), vector.getY(), vector.getZ(), null, null);
  }

  @Override
  public org.bukkit.util.Vector convertFromBezierVector(BezierVector bezierVector) {
    return new Vector(bezierVector.getX(), bezierVector.getY(), bezierVector.getZ());
  }
}
