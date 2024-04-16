package de.cubbossa.pathfinder.graph;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class ContractionHierarchies<N, E> implements PathSolver<N, E> {

  @RequiredArgsConstructor
  class Node implements Comparable<Node> {
    final N node;
    int priority = 0;
    int edgeDiff = 0;
    boolean settled = false;
    Node parent = null;
    double distance = Integer.MAX_VALUE;

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      Node node1 = (Node) o;
      return Objects.equals(node, node1.node) && Objects.equals(edgeDiff, node1.edgeDiff);
    }

    @Override
    public int hashCode() {
      return Objects.hash(node, edgeDiff);
    }

    @Override
    public String toString() {
      return node.toString();
    }

    @Override
    public int compareTo(@NotNull ContractionHierarchies<N, E>.Node o) {
      if (edgeDiff != o.edgeDiff) {
        return edgeDiff > o.edgeDiff ? 1 : -1;
      }
      return node.toString().compareTo(o.node.toString());
    }
  }

  private MutableValueGraph<Node, Double> graph = null;
  private MutableValueGraph<Node, Double> optimized = null;
  private Map<N, Node> mapping;

  // used for pass method
  private FibonacciHeap<Node> queue = new FibonacciHeap<>();

  public ContractionHierarchies() {
    mapping = new HashMap<>();
  }

  public void setGraph(ValueGraph<N, E> graph) {
    populate(graph);
    contractNodes();
  }

  public double getEdgeValue(E edge) {
    return 1;
  }

  protected void populate(ValueGraph<N, E> graph) {
    MutableValueGraph<Node, Double> temp = ValueGraphBuilder.directed()
        .expectedNodeCount(10_000)
        .nodeOrder(ElementOrder.sorted(Node::compareTo))
        .allowsSelfLoops(false)
        .build();

    for (N n1 : graph.nodes()) {
      Node node = new Node(n1);
      mapping.put(n1, node);
      if (!temp.addNode(node)) {
        throw new IllegalArgumentException("Duplicate nodes");
      }
    }
    graph.edges().forEach(ns -> {
      Node u = mapping.get(ns.nodeU());
      Node v = mapping.get(ns.nodeV());
      double weight = getEdgeValue(graph.edgeValue(ns).orElseThrow());
      if (!graph.isDirected()) {
        temp.putEdgeValue(v, u, weight);
      }
      temp.putEdgeValue(u, v, weight);
    });

    this.optimized = ValueGraphBuilder.directed()
        .expectedNodeCount(10_000)
        .nodeOrder(ElementOrder.sorted(Node::compareTo))
        .allowsSelfLoops(false)
        .build();
    this.graph = ValueGraphBuilder.directed()
        .expectedNodeCount(10_000)
        .nodeOrder(ElementOrder.sorted(Node::compareTo))
        .allowsSelfLoops(false)
        .build();

    new ArrayList<>(temp.nodes()).forEach(node -> {

      Map<Node, Map<Node, Double>> contracted = contractNode(temp, node);
      int count = temp.adjacentNodes(node).size();
      Node n = new Node(node.node);
      mapping.put(n.node, n);
      n.edgeDiff = contracted.values().stream().mapToInt(Map::size).sum() - count;

      this.graph.addNode(n);
      this.optimized.addNode(n);
    });

//    for (Node node : new ArrayList<>(temp.nodes())) {
//      Map<Node, Map<Node, Double>> contracted = contractNode(temp, node);
//      int count = temp.adjacentNodes(node).size();
//      Node n = new Node(node.node);
//      mapping.put(n.node, n);
//      n.edgeDiff = contracted.values().stream().mapToInt(Map::size).sum() - count;
//
//      this.graph.addNode(n);
//      this.optimized.addNode(n);
//    }
    int prio = 0;
    for (Node node : optimized.nodes()) {
      node.priority = prio++;
    }
    graph.edges().forEach(ns -> {
      Node u = mapping.get(ns.nodeU());
      Node v = mapping.get(ns.nodeV());
      double weight = getEdgeValue(graph.edgeValue(ns).orElseThrow());
      if (!graph.isDirected()) {
        this.graph.putEdgeValue(v, u, weight);
        this.optimized.putEdgeValue(v, u, weight);
      }
      this.graph.putEdgeValue(u, v, weight);
      this.optimized.putEdgeValue(u, v, weight);
    });
  }

  private Map<Node, Map<Node, Double>> contractNode(ValueGraph<Node, Double> graph, Node node) {
    Map<Node, Map<Node, Double>> result = new HashMap<>();

    for (Node pre : graph.predecessors(node)) {
      for (Node suc : graph.successors(node)) {
        if (pre.equals(suc)) continue;
        if (!passes(graph, pre, suc, node)) continue;

        // there can be a shurtcut from pre to suc.
        double v1 = graph.edgeValue(pre, node).orElseThrow();
        double v2 = graph.edgeValue(node, suc).orElseThrow();
        result.computeIfAbsent(pre, n -> new HashMap<>()).put(suc, v1 + v2);
      }
    }
    return result;
  }

  protected void contractNodes() {

    // view every node in given order
    for (Node node : new ArrayList<>(optimized.nodes())) {
      Map<Node, Map<Node, Double>> contracted = contractNode(this.graph, node);
      contracted.forEach((n, weightMap) -> {
        weightMap.forEach((end, weight) -> {
          optimized.putEdgeValue(n, end, weight);
          graph.putEdgeValue(n, end, weight);
        });
      });
      graph.removeNode(node);
    }
  }

  protected boolean passes(ValueGraph<Node, Double> graph, Node start, Node end, Node passingTested) {

    double ab = graph.edgeValue(start, passingTested).orElseThrow();
    double bc = graph.edgeValue(passingTested, end).orElseThrow();
    double maxLen = ab + bc;
    Collection<Node> visited = new HashSet<>();
    start.distance = 0;
    queue.enqueue(start, 0);
    visited.add(start);

    queueLoop:
    while (!queue.isEmpty()) {
      Node current = queue.dequeueMin().getValue();
      for (Node node : graph.successors(current)) {
        if (!node.settled) {
          double d = current.distance + graph.edgeValue(current, node).orElseThrow();
          if (d > maxLen) break queueLoop;
          if (d < node.distance) {
            node.distance = d;
            node.parent = current;
            visited.add(node);
          }
          queue.enqueue(node, node.distance);
        }
      }
      current.settled = true;
      visited.add(current);
      if (current.equals(end)) {
        break;
      }
    }
    boolean result = Objects.equals(end.parent, passingTested);
    visited.forEach(node -> {
      node.settled = false;
      node.distance = Float.MAX_VALUE;
      node.parent = null;
    });
    while (!queue.isEmpty()) queue.dequeueMin();
    return result;
  }

  @Override
  public PathSolverResult<N, E> solvePath(N start, N target) throws NoPathFoundException {
    // full dijkstra on upwards optimized
    // collect all visited nodes in a set
    // full dijkstra on downwards optimized
    // if contained in set, add to map with combined dist
    // find min of map
    // -> shortest path

    Collection<Node> visited = new HashSet<>();
    Collection<Node> upwardsGraph = new HashSet<>();
    Map<Node, Node> seam = new HashMap<>();

    // upwards graph dijkstra
    queue.enqueue(mapping.get(start), 0);
    queue.min().getValue().distance = 0;
    while (!queue.isEmpty()) {
      Node current = queue.dequeueMin().getValue();
      upwardsGraph.add(current);
      visited.add(current);

      for (Node node : optimized.successors(current)) {
        if (node.settled) continue;
        if (node.priority < current.priority) continue;

        double d = current.distance + optimized.edgeValue(current, node).orElseThrow();
        if (d < node.distance) {
          node.distance = d;
          node.parent = current;
        }
        queue.enqueue(node, node.distance);
      }
      current.settled = true;
    }
    visited.forEach(node -> node.settled = false);

    // downwards graph dijkstra
    queue.enqueue(mapping.get(target), Integer.MAX_VALUE);
    queue.min().getValue().distance = 0;
    while (!queue.isEmpty()) {
      Node current = queue.dequeueMin().getValue();
      visited.add(current);

      for (Node node : optimized.successors(current)) {
        if (node.settled) continue;
        if (node.priority > current.priority && !upwardsGraph.contains(node)) continue;

        double d = current.distance + optimized.edgeValue(current, node).orElseThrow();
        if (d < node.distance) {
          if (upwardsGraph.contains(node)) {
            node.distance += d;
            seam.put(node, current);
          } else {
            node.distance = d;
            node.parent = current;
          }
        }
        queue.enqueue(node, node.distance);
      }
      current.settled = true;
    }
    visited.forEach(node -> {
      node.settled = false;
      node.distance = Float.MAX_VALUE;
      node.parent = null;
    });
    return null;
  }

  @Override
  public PathSolverResult<N, E> solvePath(N start, Collection<N> targets) throws NoPathFoundException {
    return null;
  }
}
