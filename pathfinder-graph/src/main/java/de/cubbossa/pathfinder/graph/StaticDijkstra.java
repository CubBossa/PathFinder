package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class StaticDijkstra<N, E> implements PathSolver<N, E> {

  private final Map<N, Node> nodeMapping = new HashMap<>();
  private ValueGraph<N, E> originalGraph;
  private MutableValueGraph<Node, Double> graph;
  private final Function<E, Double> costFunction;

  public StaticDijkstra(Function<E, Double> costFunction) {
    this.costFunction = costFunction;
  }

  private PathSolverResult<N, E> extractResult(Node node) {
    LinkedList<N> result = new LinkedList<>();
    LinkedList<E> edges = new LinkedList<>();
    double costs = 0;
    while (node != null) {
      result.add(0, node.getNode());
      if (node.parent != null) {
        originalGraph.edgeValue(node.parent.getNode(), node.getNode()).ifPresent(e -> {
          edges.add(0, e);
        });
        node = node.parent;
      } else {
        costs = node.distance;
      }
    }
    return new PathSolverResultImpl<>(result, edges, costs);
  }

  @Override
  public void setGraph(ValueGraph<N, E> graph) {
    this.originalGraph = graph;
    this.nodeMapping.clear();
    this.graph = ValueGraphBuilder.directed()
        .allowsSelfLoops(false)
        .build();

    graph.nodes().forEach(n -> {
      Node node = new Node(n);
      nodeMapping.put(n, node);
      this.graph.addNode(node);
    });
    graph.edges().forEach(ns -> {
      double v = getEdgeValue(graph.edgeValue(ns).orElseThrow());
      this.graph.putEdgeValue(nodeMapping.get(ns.nodeU()), nodeMapping.get(ns.nodeV()), v);
      if (!graph.isDirected()) {
        this.graph.putEdgeValue(nodeMapping.get(ns.nodeV()), nodeMapping.get(ns.nodeU()), v);
      }
    });
  }

  @Override
  public double getEdgeValue(E edge) {
    return costFunction.apply(edge);
  }

  @Override
  public PathSolverResult<N, E> solvePath(N start, Collection<N> targets) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkState(targets.size() > 0);

    FibonacciHeap<Node> unsettled = new FibonacciHeap<>();
    Node startNode = nodeMapping.get(start);
    Collection<Node> targetNodes = targets.stream().map(nodeMapping::get).collect(Collectors.toCollection(HashSet::new));
    startNode.distance = 0;
    unsettled.enqueue(startNode, 0);

    while (!unsettled.isEmpty()) {
      Node current = unsettled.dequeueMin().getValue();

      graph.successors(current).forEach(adjacent -> {
        if (!adjacent.settled) {
          double d = current.distance + graph.edgeValue(current, adjacent).orElseThrow();
          if (d < adjacent.distance) {
            adjacent.distance = d;
            adjacent.parent = current;
          }
          unsettled.enqueue(adjacent, adjacent.distance);
        }
      });
      current.settled = true;

      if (targetNodes.contains(current)) {
        break;
      }
    }

    Collection<N> filtered = targets.stream()
        .filter(nodeMapping::containsKey)
        .collect(Collectors.toCollection(HashSet::new));
    Node nearest = filtered.stream()
        .map(nodeMapping::get)
        .min(Comparator.comparingDouble(Node::getDistance))
        .orElse(null);

    if (nearest == null || !nearest.settled) {
      throw new NoPathFoundException();
    }
    return extractResult(nearest);
  }

  @Getter
  private class Node implements Comparable<Node> {
    private final N node;
    private boolean settled = false;
    private Node parent = null;
    private double distance = Integer.MAX_VALUE;

    public Node(N node) {
      this.node = node;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }

    @Override
    public int compareTo(@NotNull Node o) {
      return Double.compare(this.distance, o.distance);
    }
  }
}
