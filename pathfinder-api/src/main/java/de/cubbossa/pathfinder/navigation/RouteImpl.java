package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphUtils;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.graph.PathSolverResultImpl;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * The logic within this class is quite complex, so read the provided comments carefully.
 */
@SuppressWarnings("UnstableApiUsage")
class RouteImpl implements Route {

  /**
   * The starting point of the route. It itself must be a route, which can be
   * - another complex route with lots of substeps
   * - or a SingletonRoute that only contains one Location.
   * Routes must always start from one location, but since any Route fulfills this requirement
   * we can set a route as Route start and extract its start when needed.
   */
  private final @NotNull Route startSegment;
  /**
   * All route sub-steps except from the starting point. A substep must be a Route by itself,
   * in simple cases this might be a SingletonRoute that only contains a location.
   */
  private final @NotNull List<Collection<Route>> segmentOrder;
  /**
   * The solver that is responsible for finding the shortest path on the node graph.
   * You can set any solver, by default {@link DynamicDijkstra}
   */
  private @NotNull PathSolver<Node, Double> baseGraphSolver;

  RouteImpl(@NotNull Route other) {
    this.startSegment = other;
    this.segmentOrder = new ArrayList<>();
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  RouteImpl(@NotNull NavigationLocation start) {
    this.startSegment = new SingletonRoute(start);
    this.segmentOrder = new ArrayList<>();
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  @Override
  public @NotNull Route withSolver(@NotNull PathSolver<Node, Double> solver) {
    this.baseGraphSolver = solver;
    return this;
  }

  @Override
  public @NotNull NavigationLocation getStart() {
    return startSegment.getStart();
  }

  @Override
  public @NotNull Collection<NavigationLocation> getEnd() {
    return segmentOrder.get(segmentOrder.size() - 1).stream()
        .map(Route::getEnd)
        .flatMap(Collection::stream)
        .toList();
  }

  @Override
  @NotNull
  public Route to(@NotNull Route route) {
    segmentOrder.add(List.of(route));
    return this;
  }

  @Override
  @NotNull
  public Route to(@NotNull List<Node> route) {
    // Considering the documentation for this method, the parameter
    // is actually a Route in wrong format.
    // We just turn it into a proper Route object and add it to our segments.

    List<Double> edges = new LinkedList<>();
    double cost = 0;
    Node prev = null;
    for (Node node : route) {
      if (prev == null) {
        prev = node;
        continue;
      }
      double edge = node.getLocation().distance(prev.getLocation());
      edges.add(edge);
      cost += edge;
      prev = node;
    }
    NavigationLocation start = NavigationLocation.fixedGraphNode(route.get(0));
    NavigationLocation end = NavigationLocation.fixedGraphNode(route.get(route.size() - 1));

    double finalCost = cost;
    segmentOrder.add(List.of(new SingletonRoute(start) {

      @Override
      public @NotNull Collection<NavigationLocation> getEnd() {
        return Collections.singletonList(end);
      }

      @Override
      public PathSolverResult<Node, Double> calculatePath(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {
        return new PathSolverResultImpl<>(route, edges, finalCost);
      }
    }));
    return this;
  }

  @Override
  public @NotNull Route to(@NotNull Node node) {
    return to(NavigationLocation.fixedGraphNode(node));
  }

  @Override
  public @NotNull Route to(@NotNull NavigationLocation location) {
    segmentOrder.add(List.of(new SingletonRoute(location)));
    return this;
  }

  @Override
  public @NotNull Route toAny(Node... nodes) {
    segmentOrder.add(Arrays.stream(nodes)
        .map(NavigationLocation::fixedGraphNode)
        .map(SingletonRoute::new)
        .collect(Collectors.toList()));
    return this;
  }

  @Override
  public @NotNull Route toAny(@NotNull Collection<Node> nodes) {
    segmentOrder.add(nodes.stream()
        .map(NavigationLocation::fixedGraphNode)
        .map(SingletonRoute::new)
        .collect(Collectors.toList()));
    return this;
  }

  @Override
  public @NotNull Route toAny(NavigationLocation... locations) {
    segmentOrder.add(Arrays.stream(locations)
        .map(SingletonRoute::new)
        .collect(Collectors.toList()));
    return this;
  }

  @Override
  public @NotNull Route toAny(Route... other) {
    segmentOrder.add(Arrays.stream(other).collect(Collectors.toList()));
    return this;
  }

  @Override
  public PathSolverResult<Node, Double> calculatePath(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {
    // calculatePaths is an effort that has to be done and returns a sorted list of possible paths.
    // We rely on the fact that the list is sorted and return the first and therefore shortest route.
    // To reduce the performance impact significantly we can do a separate search on the last bit by removing it
    // before calling #calculatePaths. We only need to call #calculatePath on the last bit.

    // First of all, if segmentOrder is empty this is only a wrapper route for another route in startSegment
    if (segmentOrder.isEmpty()) {
      return startSegment.calculatePath(environment);
    }
    // Get the second last segment (might be the start segment for a simple a->b route)
    var secondLastSegment = segmentOrder.size() == 1 ? Collections.singleton(startSegment) : segmentOrder.get(segmentOrder.size() - 2);
    // Get the last segment
    var lastSegment = segmentOrder.remove(segmentOrder.size() - 1);
    var lastSegmentLocations = lastSegment.stream().flatMap(r -> r.getEnd().stream()).toList();

    // Connect all target points to a temporary graph
    var islands = GraphUtils.islands(environment);
    var outGraph = GraphUtils.merge(
        lastSegmentLocations.stream().map(n -> {
          AtomicInteger outSuccessInc = new AtomicInteger(0);
          var out = GraphUtils.merge(
              islands.stream().map(island -> connect(n, island, outSuccessInc, false, true)).toList()
          );
          if (outSuccessInc.get() == 0) {
            throw new GraphEntryNotEstablishedException();
          }
          return out;
        }).toList()
    );

    // Create a cache for all the resolved end point routes
    Map<Node, PathSolverResult<Node, Double>> shortestFromEachLast = new HashMap<>();
    for (Route route : lastSegment) {
      var r = route.calculatePath(environment);
      shortestFromEachLast.put(r.getPath().get(0), r);
    }
    // Create a list of nodes to reuse in each iteration
    var lastBitNodes = lastSegmentLocations.stream().map(NavigationLocation::getNode).toList();

    var ends = secondLastSegment.stream().map(Route::getEnd).flatMap(Collection::stream).toList();
    Map<Node, PathSolverResult<Node, Double>> shortestFromEachSecondLast = new HashMap<>();
    for (NavigationLocation start : ends) {
      AtomicInteger inSuccessInc = new AtomicInteger(0);
      var inGraph = GraphUtils.merge(
          islands.stream().map(island -> connect(start, island, inSuccessInc, true, false)).toList()
      );
      if (inSuccessInc.get() == 0) {
        throw new GraphEntryNotEstablishedException();
      }
      var combinedGraph = GraphUtils.merge(
          inGraph,
          outGraph
      );
      // Solve from end point of second last to start point of last and store result
      baseGraphSolver.setGraph(combinedGraph);
      var r = baseGraphSolver.solvePath(start.getNode(), lastBitNodes);
      shortestFromEachSecondLast.put(start.getNode(), r);
    }

    // After we have successfully mapped all shortest paths for the last bit, lets do the first bit
    var res = calculatePaths(environment);
    segmentOrder.add(lastSegment);
    if (res.isEmpty()) {
      throw new NoPathFoundException(getStart(), getEnd());
    }
    // We need to merge first bit of variable size + the interspace between last and second last route + the recursive result of last
    var result = res.iterator().next();
    var con = shortestFromEachSecondLast.get(result.getPath().get(result.getPath().size() - 1));
    return mergeResults(result, con, shortestFromEachLast.get(con.getPath().get(con.getPath().size() - 1)));
  }

  @Override
  public List<PathSolverResult<Node, Double>> calculatePaths(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {

    if (segmentOrder.isEmpty()) {
      // Special case: The start route is actually al there is. Let's return it then.
      // THis case occurs because we occasionally remove the last segments group in #calculatePath
      return startSegment.calculatePaths(environment);
    }

    // Feed the graph to the solver.
    baseGraphSolver.setGraph(environment);


    // An abstracted graph will be used to find the shortest route across all sub-steps.
    MutableValueGraph<Route, PathSolverResult<Node, Double>> abstractGraph = ValueGraphBuilder
        .directed()
        .allowsSelfLoops(false)
        .expectedNodeCount(segmentOrder.stream().mapToInt(Collection::size).sum())
        .build();
    abstractGraph.addNode(startSegment);

    // Create n*m relation edges from each segment to the next
    Collection<Route> prev = new HashSet<>();
    prev.add(startSegment);

    for (Collection<Route> segmentOptions : segmentOrder) {
      // SegmentOptions is a list of segments of which one must be visited.
      for (Route segment : segmentOptions) {
        // each end point will be its own node on the abstract graph
        abstractGraph.addNode(segment);

        // Make an edge from each previous checkpoint point to this point
        for (Route prevSegment : prev) {
          // If by accident any segment comes twice in a row we do not need to connect them
          if (prevSegment.equals(segment)) {
            continue;
          }
          // Only make edge in abstract graph if it can actually be used. Otherwise skip
          try {
            var solved = findShortestPathBetweenSegments(environment, prevSegment, segment);
            abstractGraph.putEdgeValue(prevSegment, segment, solved);
          } catch (NoPathFoundException ignored) {
            // Ignore -> edge cannot be traversed but maybe another way is still possible.
          }
        }
      }
      prev = segmentOptions;
    }

    // Once we constructed an abstract graph of segments, solve shortest path on it.
    DynamicDijkstra<Route, PathSolverResult<Node, Double>> abstractSolver = new DynamicDijkstra<>(PathSolverResult::getCost);
    abstractSolver.setGraph(abstractGraph);

    List<PathSolverResult<Node, Double>> results = new ArrayList<>();
    // Let's iterate all possible end points and solve the shortest path to them.
    for (Route end : segmentOrder.get(segmentOrder.size() - 1)) {
      try {
        // We find the shortest path on the abstract graph here to this particularly end.
        PathSolverResult<Route, PathSolverResult<Node, Double>> res = abstractSolver.solvePath(
            startSegment,
            end
        );

        var paths = end.calculatePaths(environment);

        // Since we want to return every possible path we need to iterate all ends of the end route object too
        for (PathSolverResult<Node, Double> calculatedPath : paths) {
          // We combine them into one and append them to our result list.
          results.add(mergeResults(
              flatMapAbstractResult(res),
              calculatedPath
          ));
        }
      } catch (NoPathFoundException ignored) {
      }
    }
    // Now we have all shortest paths to each possible end point of this route, including all end points
    // of sub-routes if they were endpoints themselves.

    // Now we can tell that there is no possible outcome, if there are no results.
    if (results.isEmpty()) {
      throw new NoPathFoundException(
          getStart(),
          getEnd()
      );
    }
    // Otherwise sort by costs and return.
    results.sort(Comparator.comparingDouble(PathSolverResult::getCost));
    return results;
  }


  private PathSolverResult<Node, Double> flatMapAbstractResult(PathSolverResult<Route, PathSolverResult<Node, Double>> elements) throws NoPathFoundException {

    double cost = 0;
    List<Node> path = new ArrayList<>();
    List<Double> edges = new ArrayList<>();

    for (PathSolverResult<Node, Double> abstractEdge : elements.getEdges()) {
      List<Node> n = abstractEdge.getPath();
      if (!path.isEmpty()) {
        n = n.subList(1, n.size());
      }
      path.addAll(n);
      edges.addAll(abstractEdge.getEdges());
      cost += abstractEdge.getCost();
    }
    return new PathSolverResultImpl<>(path, edges, cost);
  }

  /**
   * Takes all end points of a and the starting point of b and runs a shortest path search on the modified base graph
   *
   * @param a the lower end delimiting Route (by end locations)
   * @param b the upper end delimiting Route (by start location)
   * @return A result that contains the path from STARTING-point of "a" to starting point of "b" (inclusive)
   * This is necessary because we otherwise cannot tell which possible route of a has been taken.
   * @throws NoPathFoundException If no path was found.
   */
  private PathSolverResult<Node, Double> findShortestPathBetweenSegments(ValueGraph<Node, Double> environment, Route a, Route b) throws NoPathFoundException {
    var islands = GraphUtils.islands(environment);

    AtomicInteger inSuccessInc = new AtomicInteger(0);
    var inGraph = GraphUtils.merge(
        islands.stream().map(island -> connect(b.getStart(), island, inSuccessInc, false, true)).toList()
    );
    if (inSuccessInc.get() == 0) {
      throw new GraphEntryNotEstablishedException();
    }
    environment = GraphUtils.merge(
        inGraph,
        GraphUtils.merge(
            a.getEnd().stream().map(n -> {
              AtomicInteger outSuccessInc = new AtomicInteger(0);
              var outGraph = GraphUtils.merge(
                  islands.stream().map(island -> connect(n, island, outSuccessInc, true, false)).toList()
              );
              if (outSuccessInc.get() == 0) {
                throw new GraphEntryNotEstablishedException();
              }
              return outGraph;
            }).toList()
        )
    );
    baseGraphSolver.setGraph(environment);

    List<PathSolverResult<Node, Double>> resolvedPathsOfA = a.calculatePaths(environment);
    List<PathSolverResult<Node, Double>> results = new ArrayList<>();

    Node end = b.getStart().getNode();
    for (NavigationLocation startLoc : a.getEnd()) {
      Node start = startLoc.getNode();

      // TODO use a map for performance in the first place
      PathSolverResult<Node, Double> resultOfA = resolvedPathsOfA.stream()
          .filter(r -> r.getPath().get(r.getPath().size() - 1).equals(start))
          .findAny()
          .orElseThrow();
      results.add(mergeResults(resultOfA, baseGraphSolver.solvePath(start, end)));

    }
    if (results.isEmpty()) {
      throw new NoPathFoundException(a, b);
    }
    results.sort(Comparator.comparingDouble(PathSolverResult::getCost));
    return results.get(0);
  }

  private ValueGraph<Node, Double> connect(NavigationLocation location, ValueGraph<Node, Double> graph, AtomicInteger successInc, boolean entry, boolean exit) {
    try {
      if (entry) {
        if (exit) {
          graph = location.connect(graph);
        } else {
          graph = location.connectAsEntry(graph);
        }
      } else {
        if (exit) {
          graph = location.connectAsExit(graph);
        } else {
          throw new IllegalStateException("Either entry or exit must be true");
        }
      }
      successInc.getAndIncrement();
    } catch (GraphEntryNotEstablishedException ignored) {
    }
    return graph;
  }

  @SafeVarargs
  private PathSolverResult<Node, Double> mergeResults(PathSolverResult<Node, Double>... results) {
    return mergeResults(Arrays.stream(results).toList());
  }

  private PathSolverResult<Node, Double> mergeResults(Iterable<PathSolverResult<Node, Double>> iterable) {
    boolean first = true;

    List<Node> nodePath = new ArrayList<>();
    List<Double> edges = new ArrayList<>();
    double cost = 0;
    for (PathSolverResult<Node, Double> result : iterable) {
      if (!first && !result.getPath().isEmpty()) {
        nodePath.addAll(result.getPath().subList(1, result.getPath().size()));
      } else {
        nodePath.addAll(result.getPath());
        first = false;
      }
      edges.addAll(result.getEdges());
      cost += result.getCost();
    }
    return new PathSolverResultImpl<>(nodePath.stream()
//        .map(n -> modifiedBaseGraph.nodes().stream().filter(o -> n.getNodeId().equals(o.getNodeId())).findAny().orElseThrow())
        .toList(), edges, cost);
  }
}
