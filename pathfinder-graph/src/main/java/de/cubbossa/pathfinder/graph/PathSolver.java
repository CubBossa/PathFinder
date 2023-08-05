package de.cubbossa.pathfinder.graph;

import com.google.common.graph.ValueGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An algorithm to find a path on a graph. This might be the shortest possible path, but
 * it might also be a trade-off where the outcome is not guaranteed to be the shortest path to increase performance.
 *
 * @param <N> The type that represents one node of the graph.
 */
public interface PathSolver<N> {

  /**
   * Sets the graph instance where the search should be run upon. Some pathfinding
   * algorithms might need to translate the graph into an internal representation of nodes.
   * To reduce the cost of individual searches on the same graph, the graph must be set separately.
   *
   * @param graph A graph where edges are weighted by doubles.
   */
  void setGraph(ValueGraph<N, Double> graph);

  /**
   * Find a path from one start location to one target location.
   *
   * @param start  The start node to navigate from.
   * @param target The target node to find a path to.
   * @return A list of node instances that resemble the path. The first node will be the
   * start node instance, the last node the target.
   * @throws NoPathFoundException if the solver could not find any path from start to target node.
   */
  default List<N> solvePath(N start, N target) throws NoPathFoundException {
    return solvePath(start, Collections.singleton(target));
  }

  /**
   * Find a path from one start location to any of the given target locations.
   *
   * @param start   The start node to navigate from.
   * @param targets The target nodes to find a path to. The path must reach one of the given target nodes.
   * @return A list of node instances that resemble the path. The first node will be the
   * start node instance, the last node the first matching target that was found.
   * @throws NoPathFoundException if the solver could not find any path from start to any target node.
   */
  List<N> solvePath(N start, Collection<N> targets) throws NoPathFoundException;
}
