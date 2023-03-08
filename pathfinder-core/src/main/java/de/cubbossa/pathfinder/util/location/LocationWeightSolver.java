package de.cubbossa.pathfinder.util.location;

import java.util.Map;

public interface LocationWeightSolver<T> {

  Map<T, Double> solve (T location, Iterable<T> scope);
}
