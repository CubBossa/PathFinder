package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.graph.DynamicDijkstra;
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
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RouteImpl implements Route {

  private final List<Collection<Object>> targets;
  private PathSolver<Node, Double> baseGraphSolver;

  // caching
  private @Nullable ValueGraph<Node, Double> modifiedBaseGraph = null;

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
  public Route withSolver(@NotNull PathSolver<Node, Double> solver) {
    this.baseGraphSolver = solver;
    return this;
  }

  @Override
  public @NotNull NavigationLocation getStart() {
    Object first = targets.get(0).stream().findAny().orElse(null);
    if (first instanceof NavigationLocation loc) {
      return loc;
    }
    throw new IllegalStateException("Invalid first node: " + first);
  }

  @Override
  public @NotNull Collection<NavigationLocation> getEnd() {
    return targets.get(targets.size() - 1).stream()
        .filter(object -> object instanceof NavigationLocation)
        .map(object -> (NavigationLocation) object)
        .toList();
  }

  @Override
  public Route to(@NotNull Route route) {
    targets.add(List.of(route));
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
    targets.add(List.of(new RouteEl(route.get(0), route.get(route.size() - 1)) {
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
    targets.add(List.of(location));
    return this;
  }

  @Override
  public Route toAny(Node... nodes) {
    targets.add(Arrays.stream(nodes).map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(@NotNull Collection<Node> nodes) {
    targets.add(nodes.stream().map(this::loc).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(NavigationLocation... locations) {
    targets.add(Arrays.stream(locations).collect(Collectors.toList()));
    return this;
  }

  @Override
  public Route toAny(Route... other) {
    targets.add(Arrays.stream(other).collect(Collectors.toList()));
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
    modifiedBaseGraph = prepareBaseGraph(environment);
    baseGraphSolver.setGraph(modifiedBaseGraph);

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
          // Only make edge in abstract graph if it can actually be used. Otherwise skip
          try {
            var solved = solveForSection(p, inner);
            abstractGraph.putEdgeValue(p, inner, solved);
          } catch (NoPathFoundException e) {
            System.out.println("No path found from " + p.end + " to " + inner.start);
            for (EndpointPair<Node> edge : modifiedBaseGraph.edges()) {
              System.out.println(edge.source().getNodeId() + " " + edge.source().getLocation().getX() + " -> " + edge.target().getNodeId() + " " + edge.target().getLocation().getX());
            }
          }
        }
        prev = target;
      }
    }

    DynamicDijkstra<RouteEl, PathSolverResult<Node, Double>> abstractSolver = new DynamicDijkstra<>(PathSolverResult::getCost);
    abstractSolver.setGraph(abstractGraph);

    List<PathSolverResult<Node, Double>> results = new ArrayList<>();
    var start = convertedTargets.get(0).iterator().next();
    for (RouteEl lastTarget : convertedTargets.get(convertedTargets.size() - 1)) {
      try {
        PathSolverResult<RouteEl, PathSolverResult<Node, Double>> res = abstractSolver.solvePath(
            start,
            lastTarget
        );
        results.add(join(res));
      } catch (Throwable t) {
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

  private Collection<RouteEl> newElement(Object o, ValueGraph<Node, Double> environment) throws NoPathFoundException {
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

  private ValueGraph<Node, Double> prepareBaseGraph(ValueGraph<Node, Double> graph) {
    MutableValueGraph<Node, Double> g;
    if (graph instanceof MutableValueGraph<Node, Double> mGraph) {
      g = mGraph;
    } else {
      g = ValueGraphBuilder.from(graph).build();
      graph.nodes().forEach(g::addNode);
      for (EndpointPair<Node> e : graph.edges()) {
        g.putEdgeValue(e.nodeU(), e.nodeV(), g.edgeValue(e.nodeU(), e.nodeV()).orElse(0d));
      }
    }

    for (Collection<Object> target : targets) {
      for (Object o : target) {
        if (o instanceof NavigationLocation navLoc) {
          g = navLoc.connect(g);
        }
      }
    }
    return g;
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
