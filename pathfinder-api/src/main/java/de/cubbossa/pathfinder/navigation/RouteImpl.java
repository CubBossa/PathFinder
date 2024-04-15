package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class RouteImpl implements Route {

  private final List<Collection<Object>> targets;
  private final PathSolver<Node, Double> baseGraphSolver;

  RouteImpl(Route other) {
    this.targets = new ArrayList<>();
    this.targets.add(List.of(other));
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  RouteImpl(NavigationLocation start) {
    this.targets = new ArrayList<>();
    this.targets.add(List.of(start));
    this.baseGraphSolver = new DynamicDijkstra<>(Function.identity());
  }

  private NavigationLocation loc(Node node) {
    if (node instanceof NavigationLocation nav) {
      return nav;
    }
    return NavigationLocation.fixedGraphNode(node);
  }

  @Override
  public NavigationLocation getStart() {
    Object first = targets.get(0).stream().findAny().orElse(null);
    if (first instanceof NavigationLocation loc) {
      return loc;
    }
    throw new IllegalStateException("Invalid first node: " + first);
  }

  @Override
  public Collection<NavigationLocation> getEnd() {
    return targets.get(targets.size() - 1).stream()
        .filter(object -> object instanceof NavigationLocation)
        .map(object -> (NavigationLocation) object)
        .toList();
  }

  @Override
  public Route to(Route route) {
    targets.add(List.of(route));
    return this;
  }

  @Override
  public Route to(List<Node> route) {
    if (route.isEmpty()) {
      throw new IllegalArgumentException("Cannot create empty route. No targets provided.");
    }
    Route r = new RouteImpl(loc(route.get(0)));
    for (Node node : route.subList(1, route.size())) {
      r = r.to(node);
    }
    return to(r);
  }

  @Override
  public Route to(Node node) {
    return to(loc(node));
  }

  @Override
  public Route to(NavigationLocation location) {
    targets.add(List.of(location));
    return this;
  }

  @Override
  public Route toAny(Node... nodes) {
    targets.add(Arrays.stream(nodes).map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(Collection<Node> nodes) {
    targets.add(nodes.stream().map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(NavigationLocation... locations) {
    targets.add(Arrays.stream(locations).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(String searchString) {
    return null;
  }

  @Override
  public Route toAny(Route... other) {
    targets.add(Arrays.stream(other).collect(Collectors.toList()));
    return this;
  }

  @Override
  public PathSolverResult<Node, Double> calculatePath(ValueGraph<Node, Double> environment) throws NoPathFoundException {
    var res = calculatePaths(environment);
    if (res.isEmpty()) {
      throw new NoPathFoundException();
    }
    return res.iterator().next();
  }

  @Override
  public List<PathSolverResult<Node, Double>> calculatePaths(ValueGraph<Node, Double> environment) throws NoPathFoundException {
    baseGraphSolver.setGraph(environment);

    MutableValueGraph<RouteEl, PathSolverResult<Node, Double>> abstractGraph = ValueGraphBuilder
        .directed()
        .allowsSelfLoops(false)
        .expectedNodeCount(targets.stream().mapToInt(Collection::size).sum())
        .build();

    List<Collection<RouteEl>> convertedTargets = new ArrayList<>();
    for (Collection<Object> target : targets) {
      Collection<RouteEl> inner = new HashSet<>();
      for (Object o : target) {
        inner.addAll(newElement(o, environment));
      }
      convertedTargets.add(inner);
    }

    Collection<RouteEl> prev = new HashSet<>();
    for (Collection<RouteEl> target : convertedTargets) {
      for (RouteEl inner : target) {
        abstractGraph.addNode(inner);

        for (RouteEl p : prev) {
          abstractGraph.putEdgeValue(p, inner, solveForSection(p, inner));
        }
        prev = target;
      }
    }

    DynamicDijkstra<RouteEl, PathSolverResult<Node, Double>> abstractSolver = new DynamicDijkstra<>(PathSolverResult::getCost);
    abstractSolver.setGraph(abstractGraph);

    List<PathSolverResult<Node, Double>> results = new ArrayList<>();
    for (RouteEl lastTarget : convertedTargets.get(convertedTargets.size() - 1)) {
      PathSolverResult<RouteEl, PathSolverResult<Node, Double>> res = abstractSolver.solvePath(
          convertedTargets.get(0).iterator().next(),
          lastTarget
      );
      results.add(join(res));
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
    return merge(List.of(a.solve(), baseGraphSolver.solvePath(a.end, b.start)));
  }

  private Collection<RouteEl> newElement(Object o, ValueGraph<Node, Double> environment) throws NoPathFoundException {
    if (o instanceof NavigationLocation loc) {
      Node n = loc.getNode();
      return Collections.singleton(new RouteEl(n, n) {
        @Override
        PathSolverResult<Node, Double> solve() {
          return new PathSolverResultImpl<>(Collections.singletonList(n), Collections.emptyList(), 0);
        }
      });
    } else if (o instanceof Route route) {
      Collection<RouteEl> els = new LinkedList<>();
      for (PathSolverResult<Node, Double> result : route.calculatePaths(environment)) {
        els.add(new RouteEl(result.getPath().get(0), result.getPath().get(result.getPath().size() - 1)) {
          @Override
          PathSolverResult<Node, Double> solve() {
            return result;
          }
        });
      }
      return els;
    }
    throw new IllegalStateException("Don't know how to convert object into RouteEl");
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
