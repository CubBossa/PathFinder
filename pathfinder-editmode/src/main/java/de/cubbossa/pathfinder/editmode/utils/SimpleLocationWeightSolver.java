package de.cubbossa.pathfinder.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleLocationWeightSolver<T> implements LocationWeightSolver<T> {

  private final Function<T, Position> mapper;

  @Override
  public Map<T, Double> solve(T location, Iterable<T> scope) {

    Position startPos = mapper.apply(location);

    double nearestDistSquared = Double.MAX_VALUE;
    T nearestElement = null;

    for (T element : scope) {
      Position elementPos = mapper.apply(element);
      double distSquared = Math.pow(elementPos.x() - startPos.x(), 2)
          + Math.pow(elementPos.y() - startPos.y(), 2)
          + Math.pow(elementPos.z() - startPos.z(), 2);

      if (nearestDistSquared > distSquared) {
        nearestElement = element;
        nearestDistSquared = distSquared;
      }
    }
    if (nearestElement == null) {
      return new HashMap<>();
    }
    return Map.of(nearestElement, nearestDistSquared);
  }

  record Position(double x, double y, double z) {}
}
