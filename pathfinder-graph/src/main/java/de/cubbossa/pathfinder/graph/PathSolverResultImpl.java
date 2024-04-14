package de.cubbossa.pathfinder.graph;

import java.util.List;

public record PathSolverResultImpl<N, E>(List<N> path, List<E> edge,
                                         double cost) implements PathSolverResult<N, E> {

  @Override
  public List<N> getPath() {
    return path;
  }

  @Override
  public List<E> getEdges() {
    return edge;
  }

  @Override
  public double getCost() {
    return cost;
  }
}
