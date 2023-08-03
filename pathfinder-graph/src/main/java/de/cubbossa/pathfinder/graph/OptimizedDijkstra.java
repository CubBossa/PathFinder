package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OptimizedDijkstra<N> implements PathSolver<N> {

  Map<N, Node> nodeMapping = new HashMap<>();

  private Node toNode(Graph<N> graph, N node) {
    Node n = nodeMapping.get(node);
    if (n != null) {
      return n;
    }
    n = new Node(node) {
      @Override
      Map<Node, Double> getAdjacent() {
        Map<Node, Double> adjacent = new HashMap<>();
        graph.getEdgeMap().get(node).forEach((n, dist) -> {
          adjacent.put(toNode(graph, n), dist);
        });
        return adjacent;
      }
    };
    nodeMapping.put(node, n);
    return n;
  }

  public Map<N, Node> buildGraph(Graph<N> graph, N startNode) {
    nodeMapping.clear();
    toNode(graph, startNode);
    return nodeMapping;
  }


  private Map<N, Node> setStartNode(Graph<N> graph, N source, Collection<N> target) {
    Map<N, Node> scope = buildGraph(graph, source);
    Map<N, Node> computed = new HashMap<>();
    FibonacciHeap<Node> unsettled = new FibonacciHeap<>();
    Node startNode = scope.get(source);
    Collection<Node> targetNodes = target.stream().map(n -> toNode(graph, n)).collect(Collectors.toCollection(HashSet::new));
    startNode.distance = 0;
    unsettled.enqueue(startNode, 0);

    while (!unsettled.isEmpty()) {
      Node current = unsettled.dequeueMin().getValue();

      current.getAdjacent().forEach((node, value) -> {
        if (!node.settled) {
          setMinDist(current, node, value);
          unsettled.enqueue(node, node.distance);
        }
      });
      current.settled = true;
      computed.put(current.node, current);

      if (targetNodes.contains(current)) {
        break;
      }
    }
    return computed;
  }

  private void setMinDist(Node node, Node neighbour, double edgeWeight) {
    double d = node.distance + edgeWeight;
    if (d < neighbour.distance) {
      neighbour.distance = d;
      neighbour.parent = node;
    }
  }

  public List<N> solvePath(Graph<N> graph, N start, N target) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkNotNull(target);
    Preconditions.checkState(graph.hasNode(start));

    Map<N, Node> computedGraph = setStartNode(graph, start, Collections.singleton(target));

    if (!computedGraph.containsKey(target)) {
      throw new NoPathFoundException();
    }
    return extractResult(computedGraph.get(target));
  }

  private List<N> extractResult(Node node) {
    LinkedList<N> result = new LinkedList<>();
    while (node != null) {
      result.add(0, node.getNode());
      node = node.parent;
    }
    return result;
  }

  public List<N> solvePath(Graph<N> graph, N start, Collection<N> targets) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkState(graph.hasNode(start));
    Preconditions.checkState(targets.size() > 0);

    Map<N, Node> computedGraph = setStartNode(graph, start, targets);

    Collection<N> filtered = targets.stream()
        .filter(computedGraph::containsKey)
        .collect(Collectors.toCollection(HashSet::new));
    Node nearest = filtered.stream()
        .map(n -> computedGraph.get(n))
        .min(Comparator.comparingDouble(Node::getDistance))
        .orElse(null);
    if (nearest == null) {
      throw new NoPathFoundException();
    }
    return extractResult(nearest);
  }

  @Getter
  private abstract class Node implements Comparable<Node> {
    private final N node;
    private boolean settled = false;
    private Node parent = null;
    private double distance = Integer.MAX_VALUE;

    abstract Map<Node, Double> getAdjacent();

    public Node(N node) {
      this.node = node;
    }

    public Node(N node, double distance) {
      this.node = node;
      this.distance = distance;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }

    @Override
    public int compareTo(@NotNull OptimizedDijkstra<N>.Node o) {
      return Double.compare(this.distance, o.distance);
    }
  }
}
