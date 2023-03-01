package de.cubbossa.pathfinder.graph;

import java.util.Collection;
import java.util.List;

/**
 * An algorithm to find a path on a graph. This might be the shortest possible path, but
 * considering solutions like the A* algorithms this can also be a trade-off to increase performance.
 *
 * @param <N> The type that represents one node of the graph.
 */
public interface PathSolver<N> {

  /**
   * Find a path from one start location to one target location.
   *
   * @param graph  A graph instance to find the path on.
   * @param start  The start node to navigate from.
   * @param target The target node to find a path to.
   * @return A list of node instances that resemble the path. The first node will be the
   * start node instance, the last node the target.
   * @throws NoPathFoundException if the solver could not find any path from start to target node.
   */
  List<N> solvePath(Graph<N> graph, N start, N target) throws NoPathFoundException;

  /**
   * Find a path from one start location to any of the given target locations.
   *
   * @param graph   A graph instance to find the path on.
   * @param start   The start node to navigate from.
   * @param targets The target nodes to find a path to. The path must reach one of the given target nodes.
   * @return A list of node instances that resemble the path. The first node will be the
   * start node instance, the last node the first matching target that was found.
   * @throws NoPathFoundException if the solver could not find any path from start to any target node.
   */
  List<N> solvePath(Graph<N> graph, N start, Collection<N> targets)
      throws NoPathFoundException;
}
