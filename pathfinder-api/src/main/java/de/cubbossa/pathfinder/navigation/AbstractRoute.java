package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.graph.PathSolver;
import de.cubbossa.pathfinder.graph.PathSolverResult;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractRoute implements Route {

  private final List<Collection<Object>> targets;
  private final PathSolver<Node, Double> baseGraphSolver;

  AbstractRoute(Route other) {
    this.targets = new ArrayList<>();
    this.targets.add(List.of(other));
    this.baseGraphSolver = new DynamicDijkstra<>();
  }

  AbstractRoute(NavigationLocation start) {
    this.targets = new ArrayList<>();
    this.targets.add(List.of(start));
    this.baseGraphSolver = new DynamicDijkstra<>();
  }

  private NavigationLocation location(Node node) {

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
    Route r = new AbstractRoute(location(route.get(0)));
    for (Node node : route.subList(1, route.size())) {
      r = r.to(node);
    }
    return to(r);
  }

  @Override
  public Route to(Node node) {
    return to(location(node));
  }

  @Override
  public Route to(NavigationLocation location) {
    targets.add(List.of(location));
    return this;
  }

  @Override
  public Route toAny(Node... nodes) {
    targets.add(Arrays.stream(nodes).map(this::location).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(Collection<Node> nodes) {
    targets.add(nodes.stream().map(this::location).collect(Collectors.toList()));
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
    return calculatePaths(environment).iterator().next();
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
        inner.addAll(resolve(o, environment));
      }
      convertedTargets.add(inner);
    }

    Collection<RouteEl> prev = new HashSet<>();
    for (Collection<RouteEl> target : convertedTargets) {
      for (RouteEl inner : target) {
        abstractGraph.addNode(inner);

        for (RouteEl p : prev) {
          abstractGraph.putEdgeValue(p, inner, costs(p, inner));
        }
        prev = target;
      }
    }

    DynamicDijkstra<RouteEl> abstractSolver = new DynamicDijkstra<>();
    abstractSolver.setGraph(abstractGraph);

    List<PathSolverResult<Node, Double>> results = new ArrayList<>();
    for (RouteEl lastTarget : convertedTargets.get(convertedTargets.size() - 1)) {
      PathSolverResult<RouteEl, PathSolverResult<Node, Double>> res = abstractSolver.solvePath(
          convertedTargets.get(0).iterator().next(),
          lastTarget
      );
      List<Node> nodePath = new ArrayList<>();
      List<Double> edges = new ArrayList<>();
      double cost = 0;
      for (PathSolverResult<Node, Double> edge : res.getEdges()) {
        nodePath.addAll(edge.getPath());
        edges.addAll(edge.getEdges());
        cost += edge.getCost();
      }
      results.add(new PathSolverResultImpl(nodePath, edges, cost));
    }
    if (results.isEmpty()) {
      throw new NoPathFoundException();
    }
    results.sort(Comparator.comparingDouble(PathSolverResult::getCost));
    return results;
  }

  record PathSolverResultImpl(List<Node> path, List<Double> edge,
                              double cost) implements PathSolverResult<Node, Double> {

    @Override
    public List<Node> getPath() {
      return path;
    }

    @Override
    public List<Double> getEdges() {
      return edge;
    }

    @Override
    public double getCost() {
      return cost;
    }
  }

  private PathSolverResult<Node, Double> costs(RouteEl a, RouteEl b) throws NoPathFoundException {
    return baseGraphSolver.solvePath(a.end, b.start);
  }

  private Collection<RouteEl> resolve(Object o, ValueGraph<Node, Double> environment) throws NoPathFoundException {
    if (o instanceof NavigationLocation loc) {
      Node n = loc.getNode();
      return Collections.singleton(new RouteEl(n, n, 0));
    } else if (o instanceof Route route) {
      Collection<RouteEl> els = new LinkedList<>();
      for (PathSolverResult<Node, Double> result : route.calculatePaths(environment)) {
        els.add(new RouteEl(result.getPath().get(0), result.getPath().get(result.getPath().size() - 1), result.getCost()));
      }
      return els;
    }
    throw new IllegalStateException("Don't know how to convert object into RouteEl");
  }

  private record RouteEl(Node start, Node end, double cost) {
  }
}
