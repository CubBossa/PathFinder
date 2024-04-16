package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.ValueGraph;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Paths across graphs can be described with Routes. A Route is a chain of targets to visit.
 * The first element must be a single element, but every other target can be a list of targets, of which the most
 * optimal will be picked.
 * A route "a -> (b or c) -> (d or e)" will choose between b or c and accordingly d or e so that the calculated path
 * is the shortest among all possible paths.
 * <br/><br/>
 * Routes can embed other Routes. The embedded Route will be resolved first and then treated as edge on the meta graph.
 * if r is a Route and "a -> (r or b) -> c" is its parent Route, r will be used if the path along r is shorter than
 * visiting b.
 * <br/><br/>
 * Routes will automatically be shortened.
 * "a -> a -> a" will result in a Node path "a".
 * if "r = a -> b", "a -> r -> c" will result in "a -> b -> c", not in "a -> a -> b -> c".
 */
public interface Route {

  /**
   * Creates a new route object with the provided location as start point.
   * @param location A NavigateLocation to describe the start point.
   * @return A new Route. Use any "to"-method to describe it further.
   */
  static @NotNull Route from(final @NotNull NavigationLocation location) {
    return new RouteImpl(location);
  }

  /**
   * Creates a new route object with the provided location as start point.
   * @param fixedGraphNode A Node that will be treated as a fixed Node that is already part of the solving graph.
   *                       To change this behaviour, either use {@link #from(NavigationLocation)} or let the Node implement NavigateLocation.
   * @return A new Route. Use any "to"-method to describe it further.
   */
  static @NotNull Route from(final @NotNull Node fixedGraphNode) {
    if (fixedGraphNode instanceof NavigationLocation nav) {
      return from(nav);
    }
    return from(NavigationLocation.fixedGraphNode(fixedGraphNode));
  }

  /**
   * Creates a new route object with the provided location as start point.
   * @param route The other route will provide a start point.
   * @return A new Route. Use any "to"-method to describe it further.
   */
  static @NotNull Route from(final @NotNull Route route) {
    return new RouteImpl(route);
  }

  /**
   * The entry point of this Route. This is a single point. If you do need to find the shortest path among multiple
   * entry points, use one entry point that is connected to all actual entry points with an edge weight of one.
   * After solving, drop the first Node.
   * @return The entry point of the route.
   */
  @NotNull NavigationLocation getStart();

  /**
   * All possible target locations of this Route.
   * @return A immutable copy of all target locations of this Route.
   */
  @NotNull Collection<NavigationLocation> getEnd();

  /**
   * Provide a solver for the path segments.
   * The route will need to find the shortest path from one target to another and by default uses a {@link de.cubbossa.pathfinder.graph.DynamicDijkstra}.
   * If you prefer a different solving mechanic you can provide it here. Keep in mind that edges can be weighted, so that
   * implementations like AStar might fail on Portals or Highways.
   * @param solver A new solver instance
   * @return this Route instance.
   */
  Route withSolver(final @NotNull PathSolver<Node, Double> solver);

  Route to(final @NotNull Route route);

  Route to(final @NotNull List<Node> nodes);

  Route to(final @NotNull Node node);

  Route to(final @NotNull NavigationLocation location);

  Route toAny(final @NotNull Node... nodes);

  Route toAny(final @NotNull Collection<Node> nodes);

  Route toAny(final @NotNull NavigationLocation... locations);

  Route toAny(final @NotNull String searchString);

  Route toAny(final @NotNull Route... other);

  PathSolverResult<Node, Double> calculatePath(final @NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException;

  List<PathSolverResult<Node, Double>> calculatePaths(final @NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException;
}
