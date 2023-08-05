package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import com.google.common.graph.ValueGraph;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicDijkstra<N> implements PathSolver<N> {

  private final Map<N, Node> nodeMapping = new HashMap<>();
  private ValueGraph<N, Double> graph;

  private List<N> extractResult(Node node) {
    LinkedList<N> result = new LinkedList<>();
    while (node != null) {
      result.add(0, node.getNode());
      node = node.parent;
    }
    return result;
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
  public void setGraph(ValueGraph<N, Double> graph) {
    this.nodeMapping.clear();
    this.graph = graph;
  }

  @Override
  public List<N> solvePath(N start, Collection<N> targets) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkState(targets.size() > 0);
    Preconditions.checkState(targets.size() == targets.stream().filter(Objects::nonNull).toList().size());

    FibonacciHeap<Node> unsettled = new FibonacciHeap<>();
    Node startNode = node(start);
    Collection<Node> targetNodes = targets.stream()
        .filter(Objects::nonNull)
        .map(this::node).collect(Collectors.toCollection(HashSet::new));
    startNode.distance = 0;
    unsettled.enqueue(startNode, 0);

    System.out.println(System.currentTimeMillis() + " Dijkstra start");
    while (!unsettled.isEmpty()) {
      Node current = unsettled.dequeueMin().getValue();
      // actually, no settled node can end up in the queue but for some reason it happens anyways
      if (current.settled) continue;

      graph.successors(current.node).forEach(adjacent -> {
        Node adjacentNode = node(adjacent);
        if (adjacentNode.equals(current)) return;
        if (adjacentNode.settled) return;

        double d = current.distance + graph.edgeValue(current.node, adjacent).orElseThrow();
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
    System.out.println(System.currentTimeMillis() + " Dijkstra end");

    Node nearest = targetNodes.stream()
        .min(Comparator.comparingDouble(Node::getDistance))
        .orElse(null);

    if (nearest == null || !nearest.settled) {
      throw new NoPathFoundException();
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
