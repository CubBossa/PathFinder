package de.cubbossa.pathapi.misc;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public interface LocationWeightSolverRegistry<LocationT> {

  /**
   * @param key The String representation of a solver.
   * @return The solver factory that has been registered for this key.
   * @throws NoSuchElementException if there is no mapping for this key.
   */
  Supplier<LocationWeightSolver<LocationT>> get(String key);

  /**
   * @param key    A String representation for the given type of solver.
   * @param solver A factory for a certain type of solvers.
   * @throws IllegalArgumentException if another solver with this key already exists.
   */
  void register(String key, Supplier<LocationWeightSolver<LocationT>> solver);

  /**
   * Retrieves the solver that has been registered for this key and produces an instance.
   *
   * @param key A String representation for the given type of solver.
   * @return The produced instance.
   * @throws NoSuchElementException if there is no mapping for this key.
   */
  LocationWeightSolver<LocationT> produce(String key);
}
