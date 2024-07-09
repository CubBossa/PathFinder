package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import com.google.common.graph.ValueGraph;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public class DynamicDijkstra<N, E> implements PathSolver<N, E> {

  private final Map<N, Node> nodeMapping = new HashMap<>();
  private ValueGraph<N, E> graph;
  private final Function<E, Double> valueFunction;

  public DynamicDijkstra(Function<E, Double> valueFunction) {
    this.valueFunction = valueFunction;
  }

  private PathSolverResult<N, E> extractResult(Node node) {
    double cost = node.distance;
    LinkedList<N> nodes = new LinkedList<>();
    LinkedList<E> edges = new LinkedList<>();
    while (node != null) {
      nodes.add(0, node.getNode());
      if (node.parent != null) {
        graph.edgeValue(node.parent.node, node.node).ifPresent(e -> edges.add(0, e));
      }
      node = node.parent;
    }
    return new PathSolverResult<>() {
      @Override
      public List<N> getPath() {
        return nodes;
      }

      @Override
      public List<E> getEdges() {
        return edges;
      }

      @Override
      public double getCost() {
        return cost;
      }
    };
  }

  private Node node(N n) {
    Node node = nodeMapping.get(n);
    if (node != null) {
      return node;
    }
    node = new Node(n);
    nodeMapping.put(n, node);
    return node;
  }

  @Override
  public void setGraph(ValueGraph<N, E> graph) {
    this.nodeMapping.clear();
    this.graph = graph;
  }

  public double getEdgeValue(E edge) {
    return valueFunction.apply(edge);
  }

  @Override
  public PathSolverResult<N, E> solvePath(N start, Collection<N> targets) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkState(!targets.isEmpty());
    Preconditions.checkState(targets.size() == targets.stream().filter(Objects::nonNull).toList().size());

    nodeMapping.values().forEach(node -> {
      node.settled = false;
      node.parent = null;
      node.distance = Integer.MAX_VALUE;
    });

    FibonacciHeap<Node> unsettled = new FibonacciHeap<>();
    Node startNode = node(start);
    Collection<Node> targetNodes = targets.stream()
        .filter(Objects::nonNull)
        .map(this::node).collect(Collectors.toCollection(HashSet::new));
    startNode.distance = 0;
    unsettled.enqueue(startNode, 0);

    while (!unsettled.isEmpty()) {
      Node current = unsettled.dequeueMin().getValue();

      graph.successors(current.node).forEach(adjacent -> {
        Node adjacentNode = node(adjacent);
        if (adjacentNode.equals(current)) return;
        if (adjacentNode.settled) return;

        double d = current.distance + getEdgeValue(graph.edgeValue(current.node, adjacent).orElseThrow());
        if (d < adjacentNode.distance) {
          adjacentNode.distance = d;
          adjacentNode.parent = current;
        }
        unsettled.enqueue(adjacentNode, adjacentNode.distance);
      });
      current.settled = true;

      if (targetNodes.contains(current)) {
        break;
      }
    }

    Node nearest = targetNodes.stream()
        .min(Comparator.comparingDouble(Node::getDistance))
        .orElse(null);

    if (nearest == null || !nearest.settled) {
      throw new NoPathFoundException(start, targets);
    }
    return extractResult(nearest);
  }

  @Getter
  private class Node {
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
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Node node1 = (Node) o;
      return Objects.equals(getNode(), node1.getNode());
    }

    @Override
    public String toString() {
      return node.toString();
    }
  }
}
