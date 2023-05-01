package de.cubbossa.pathfinder.util.location;

import de.cubbossa.pathfinder.util.LocationWeightSolver;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class SimpleLocationWeightSolver<T> implements LocationWeightSolver<T> {

  private final int connections;
  private final Function<T, Position> mapper;

  @Override
  public Map<T, Double> solve(T location, Iterable<T> scope) {

    Position startPos = mapper.apply(location);

    TreeMap<Double, T> elements = new TreeMap<>();
    for (T element : scope) {
      Position elementPos = mapper.apply(element);
      double distSquared = Math.pow(elementPos.x() - startPos.x(), 2)
          + Math.pow(elementPos.y() - startPos.y(), 2)
          + Math.pow(elementPos.z() - startPos.z(), 2);
      elements.put(distSquared, element);
    }
    int i = 0;
    Map<T, Double> result = new HashMap<>();
    for (Map.Entry<Double, T> tEntry : elements.entrySet()) {
      if (i >= connections) {
        break;
      }
      result.put(tEntry.getValue(), tEntry.getKey());
    }
    return result;
  }

  record Position(double x, double y, double z) {
  }
}
