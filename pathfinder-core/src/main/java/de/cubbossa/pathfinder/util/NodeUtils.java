package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.splinelib.util.BezierVector;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class NodeUtils {

  public static List<BezierVector> toSpline(LinkedHashMap<Node, Double> path,
                                            boolean shortenIfOverlapping) {

    //TODO List<Collection<BezierVector>> für alle Weltsprünge neue splines

    if (path.size() <= 1) {
      throw new IllegalArgumentException("Path to modify must have at least two points.");
    }
    List<BezierVector> vectors = new ArrayList<>();


    Node first = path.keySet().iterator().next();
    vectors.add(new BezierVector(
        PathPlugin.SPLINES.convertToVector(first.getLocation()),
        PathPlugin.SPLINES.convertToVector(first.getLocation()),
        PathPlugin.SPLINES.convertToVector(first.getLocation())));

    Node prev = null;
    double sPrev = 1;
    Node curr = null;
    double sCurr = 1;
    Vector vNext = null;


    for (Map.Entry<Node, Double> entry : path.entrySet()) {
      Node next = entry.getKey();
      Double sNext = entry.getValue();
      if (prev != null) {
        vectors.add(toBezierVector(prev, curr, next, sPrev, sCurr, sNext, shortenIfOverlapping ?
            new TangentModifier(1, 0) : null));
      }
      prev = curr;
      sPrev = sCurr;
      curr = next;
      sCurr = sNext;
      vNext = next.getLocation();
    }
    vectors.add(new BezierVector(
        PathPlugin.SPLINES.convertToVector(vNext),
        PathPlugin.SPLINES.convertToVector(vNext),
        PathPlugin.SPLINES.convertToVector(vNext)));
    return vectors;
  }

  public static BezierVector toBezierVector(
      Node previous, Node current, Node next,
      double strengthPrevious, double strengthCurrent, double strengthNext,
      @Nullable TangentModifier tangentModifier) {
    Vector vPrevious = previous.getLocation();
    Vector vCurrent = current.getLocation();
    Vector vNext = next.getLocation();

    // make both same distance to vCurrent
    vPrevious = vCurrent.clone().add(vPrevious.clone().subtract(vCurrent).normalize());
    vNext = vCurrent.clone().add(vNext.clone().subtract(vCurrent).normalize());

    // dir is now independent of the distance to neighbouring points
    Vector dir = vNext.clone().subtract(vPrevious).normalize();
    double sCurrentPrev = strengthCurrent;
    double sCurrentNext = strengthCurrent;

    if (tangentModifier != null) {
      double distPrevious = vCurrent.distance(previous.getLocation());
      if (sCurrentPrev + strengthPrevious > distPrevious) {
        sCurrentPrev = distPrevious * sCurrentPrev / (strengthPrevious + sCurrentPrev
            + tangentModifier.staticOffset()) * tangentModifier.relativeOffset();
      }
      double distNext = vCurrent.distance(next.getLocation());
      if (sCurrentNext + strengthNext > distNext) {
        sCurrentNext =
            distNext * sCurrentNext / (strengthNext + sCurrentNext + tangentModifier.staticOffset())
                * tangentModifier.relativeOffset();
      }
    }

    return new BezierVector(
        PathPlugin.SPLINES.convertToVector(vCurrent),
        PathPlugin.SPLINES.convertToVector(
            vCurrent.clone().add(dir.clone().multiply(-1 * sCurrentPrev))),
        PathPlugin.SPLINES.convertToVector(vCurrent.clone().add(dir.multiply(sCurrentNext)))
    );
  }

  public record TangentModifier(double relativeOffset, double staticOffset) {
  }
}
