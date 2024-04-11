package de.cubbossa.pathfinder.graph;

import java.util.List;

public interface PathSolverResult<N, E> {

  List<N> getPath();

  List<E> getEdges();

  double getCost();
}
