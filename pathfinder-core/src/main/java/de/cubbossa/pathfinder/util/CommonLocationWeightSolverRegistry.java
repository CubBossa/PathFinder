package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.LocationWeightSolver;
import de.cubbossa.pathapi.misc.LocationWeightSolverRegistry;

import java.util.HashMap;

public class CommonLocationWeightSolverRegistry<LocationT> extends HashMap<String, LocationWeightSolver<LocationT>> implements LocationWeightSolverRegistry<LocationT> {

  public static final String KEY_SIMPLE = "SIMPLE";
  public static final String KEY_RAYCAST = "RAYCAST";

  @Override
  public LocationWeightSolver<LocationT> get(String key) {
    return super.get(key);
  }
}
