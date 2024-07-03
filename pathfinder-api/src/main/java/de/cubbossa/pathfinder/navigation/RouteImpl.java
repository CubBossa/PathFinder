package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphUtils;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.graph.PathSolverResultImpl;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
class RouteImpl implements Route {

  private final List<Collection<Object>> checkPoints;
  private PathSolver<Node, Double> baseGraphSolver;

  // caching
  private @Nullable ValueGraph<Node, Double> modifiedBaseGraph = null;

  RouteImpl(Route other) {
    this.checkPoints = new ArrayList<>();
    this.checkPoints.add(List.of(other));
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  RouteImpl(NavigationLocation start) {
    this.checkPoints = new ArrayList<>();
    this.checkPoints.add(List.of(start));
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  private NavigationLocation loc(Node node) {
    if (node instanceof NavigationLocation nav) {
      return nav;
    }
    return NavigationLocation.fixedGraphNode(node);
  }

  @Override
  public Route withSolver(@NotNull PathSolver<Node, Double> solver) {
    this.baseGraphSolver = solver;
    return this;
  }

  @Override
  public @NotNull NavigationLocation getStart() {
    Object first = checkPoints.get(0).stream().findAny().orElse(null);
    if (first instanceof NavigationLocation loc) {
      return loc;
    }
    throw new IllegalStateException("Invalid first node: " + first);
  }

  @Override
  public @NotNull Collection<NavigationLocation> getEnd() {
    return checkPoints.get(checkPoints.size() - 1).stream()
        .filter(object -> object instanceof NavigationLocation)
        .map(object -> (NavigationLocation) object)
        .toList();
  }

  @Override
  public Route to(@NotNull Route route) {
    checkPoints.add(List.of(route));
    return this;
  }

  @Override
  public Route to(@NotNull List<Node> route) {

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

    double finalCost = cost;
    checkPoints.add(List.of(new RouteEl(route.get(0), route.get(route.size() - 1)) {
      @Override
      PathSolverResult<Node, Double> solve() {
        return new PathSolverResultImpl<>(route, edges, finalCost);
      }
    }));
    return this;
  }

  @Override
  public Route to(@NotNull Node node) {
    return to(loc(node));
  }

  @Override
  public Route to(@NotNull NavigationLocation location) {
    checkPoints.add(List.of(location));
    return this;
  }

  @Override
  public Route toAny(Node... nodes) {
    checkPoints.add(Arrays.stream(nodes).map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(@NotNull Collection<Node> nodes) {
    checkPoints.add(nodes.stream().map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(NavigationLocation... locations) {
    checkPoints.add(Arrays.stream(locations).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(Route... other) {
    checkPoints.add(Arrays.stream(other).collect(Collectors.toList()));
    return this;
  }

  @Override
  public PathSolverResult<Node, Double> calculatePath(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {
    var res = calculatePaths(environment);
    if (res.isEmpty()) {
      throw new NoPathFoundException();
    }
    return res.iterator().next();
  }

  @Override
  public List<PathSolverResult<Node, Double>> calculatePaths(@NotNull ValueGraph<Node, Double> environment) throws NoPathFoundException {

    return calculatePaths(GraphUtils.islands(environment));
  }

  private List<PathSolverResult<Node, Double>> calculatePaths(@NotNull Iterable<ValueGraph<Node, Double>> environments) throws NoPathFoundException {

    // convert all checkpoint values into RouteEl instances
    List<Collection<RouteEl>> routeElCheckPoints = new ArrayList<>();
    for (Collection<Object> target : checkPoints) {
      Collection<RouteEl> inner = new HashSet<>();
      for (Object o : target) {
        inner.addAll(newElement(o, environments));
      }
      routeElCheckPoints.add(inner);
    }

    modifiedBaseGraph = prepareBaseGraph(environments);
    baseGraphSolver.setGraph(modifiedBaseGraph);

    MutableValueGraph<RouteEl, PathSolverResult<Node, Double>> abstractGraph = ValueGraphBuilder
        .directed()
        .allowsSelfLoops(false)
        .expectedNodeCount(checkPoints.stream().mapToInt(Collection::size).sum())
        .build();

    // Create n*m relation edges from each checkpoint to the next
    Collection<RouteEl> prev = new HashSet<>();
    for (Collection<RouteEl> checkPoint : routeElCheckPoints) {
      for (RouteEl checkPointElement : checkPoint) {
        // each end point will be its own node on the abstract graph
        abstractGraph.addNode(checkPointElement);

        // Make an edge from each previous checkpoint point to this point
        for (RouteEl previousCheckPointElement : prev) {
          if (previousCheckPointElement.equals(checkPointElement)) {
            continue;
          }
          // Only make edge in abstract graph if it can actually be used. Otherwise skip
          try {
            var solved = solveForSection(previousCheckPointElement, checkPointElement);
            abstractGraph.putEdgeValue(previousCheckPointElement, checkPointElement, solved);
          } catch (NoPathFoundException ignored) {
          }
        }
      }
      prev = checkPoint;
    }

    DynamicDijkstra<RouteEl, PathSolverResult<Node, Double>> abstractSolver = new DynamicDijkstra<>(PathSolverResult::getCost);
    abstractSolver.setGraph(abstractGraph);

    List<PathSolverResult<Node, Double>> results = new ArrayList<>();
    var start = routeElCheckPoints.get(0).iterator().next();
    for (RouteEl end : routeElCheckPoints.get(routeElCheckPoints.size() - 1)) {
      PathSolverResult<RouteEl, PathSolverResult<Node, Double>> res = abstractSolver.solvePath(
          start,
          end
      );
      try {
        results.add(join(res));
      } catch (Throwable t) {
        PathFinder.get().getLogger().log(Level.WARNING, "Error while finding shortest path", t);
      }
    }
    if (results.isEmpty()) {
      throw new NoPathFoundException();
    }
    results.sort(Comparator.comparingDouble(PathSolverResult::getCost));
    return results;
  }

  private PathSolverResult<Node, Double> merge(Iterable<PathSolverResult<Node, Double>> iterable) {
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
    return new PathSolverResultImpl<>(nodePath, edges, cost);
  }

  private PathSolverResult<Node, Double> join(PathSolverResult<RouteEl, PathSolverResult<Node, Double>> els) throws NoPathFoundException {
    List<PathSolverResult<Node, Double>> results = new LinkedList<>();
    Iterator<RouteEl> nit = els.getPath().iterator();
    Iterator<PathSolverResult<Node, Double>> eit = els.getEdges().iterator();
    if (!nit.hasNext()) {
      throw new IllegalStateException();
    }
    RouteEl el;
    while (nit.hasNext()) {
      el = nit.next();
      results.add(el.solve());
      if (eit.hasNext()) {
        results.add(eit.next());
      }
    }
    return merge(results);
  }

  private PathSolverResult<Node, Double> solveForSection(RouteEl a, RouteEl b) throws NoPathFoundException {
    Node start;
    if (!(a.end instanceof GroupedNode)) {
      start = modifiedBaseGraph.nodes().stream().filter(node -> node.getNodeId().equals(a.end.getNodeId())).findAny().get();
    } else {
      start = a.end;
    }
    Node end;
    if (!(b.start instanceof GroupedNode)) {
      end = modifiedBaseGraph.nodes().stream().filter(node -> node.getNodeId().equals(b.start.getNodeId())).findAny().get();
    } else {
      end = b.start;
    }
    return baseGraphSolver.solvePath(start, end);
  }

  private Collection<RouteEl> newElement(Object o, Iterable<ValueGraph<Node, Double>> environments) throws NoPathFoundException {
    if (o instanceof RouteEl el) {
      return List.of(el);
    } else if (o instanceof NavigationLocation loc) {
      Node n = loc.getNode();
      return Collections.singleton(new RouteEl(n, n) {
        @Override
        PathSolverResult<Node, Double> solve() {
          return new PathSolverResultImpl<>(Collections.singletonList(n), Collections.emptyList(), 0);
        }
      });
    } else if (o instanceof Route route) {
      Collection<RouteEl> els = new LinkedList<>();
      for (ValueGraph<Node, Double> environment : environments) {
        for (PathSolverResult<Node, Double> result : route.calculatePaths(environment)) {
          els.add(new RouteEl(result.getPath().get(0), result.getPath().get(result.getPath().size() - 1)) {
            @Override
            PathSolverResult<Node, Double> solve() {
              return result;
            }
          });
        }
      }
      return els;
    }
    throw new IllegalStateException("Don't know how to convert object into RouteEl");
  }

  private ValueGraph<Node, Double> prepareBaseGraph(Iterable<ValueGraph<Node, Double>> islands) {
    List<ValueGraph<Node, Double>> list = new ArrayList<>();
    islands.forEach(list::add);
    for (Collection<Object> target : checkPoints) {
      for (Object o : target) {
        if (o instanceof NavigationLocation navLoc) {
          boolean notThrown = false;
          for (int i = 0; i < list.size(); i++) {
            try {
              var res = navLoc.connect(GraphUtils.mutable(list.get(i)));
              list.set(i, res);
              notThrown = true;
            } catch (GraphEntryNotEstablishedException e) {
            }
          }
          if (!notThrown) {
            throw new GraphEntryNotEstablishedException();
          }
        }
      }
    }
    return GraphUtils.merge(list);
  }

  private static abstract class RouteEl {
    private final Node start;
    private final Node end;

    private RouteEl(Node start, Node end) {
      this.start = start;
      this.end = end;
    }

    abstract PathSolverResult<Node, Double> solve();

    public Node start() {
      return start;
    }

    public Node end() {
      return end;
    }

    @Override
    public String toString() {
      return "RouteEl[" +
          "start=" + start + ", " +
          "end=" + end + ']';
    }

    }
}
