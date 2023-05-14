package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.LocationWeightSolver;
import de.cubbossa.pathapi.misc.LocationWeightSolverRegistry;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class CommonLocationWeightSolverRegistry<LocationT> extends HashMap<String, Supplier<LocationWeightSolver<LocationT>>> implements LocationWeightSolverRegistry<LocationT> {

  public static final String KEY_SIMPLE = "SIMPLE";
  public static final String KEY_RAYCAST = "RAYCAST";

  @Override
  public Supplier<LocationWeightSolver<LocationT>> get(String key) {
    Supplier<LocationWeightSolver<LocationT>> val = super.get(key);
    if (val == null) {
      throw new NoSuchElementException("No LocationWeightSolver factory registered with key '" + key + "'.");
    }
    return val;
  }

  @Override
  public void register(String key, Supplier<LocationWeightSolver<LocationT>> solver) {
    if (super.containsKey(key)) {
      throw new IllegalArgumentException("Another LocationWeightSolver has already been registered with key '" + key + "'.");
    }
    super.put(key, solver);
  }

  @Override
  public LocationWeightSolver<LocationT> produce(String key) {
    return get(key).get();
  }
}
