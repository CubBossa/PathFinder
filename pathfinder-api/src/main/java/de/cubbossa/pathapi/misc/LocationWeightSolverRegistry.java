package de.cubbossa.pathapi.misc;

public interface LocationWeightSolverRegistry<LocationT> {

  LocationWeightSolver<LocationT> get(String key);
}
