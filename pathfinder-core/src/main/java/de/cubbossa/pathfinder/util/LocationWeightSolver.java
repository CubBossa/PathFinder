package de.cubbossa.pathfinder.util;

import java.util.Map;

public interface LocationWeightSolver<T> {

  Map<T, Double> solve(T location, Iterable<T> scope);
}
