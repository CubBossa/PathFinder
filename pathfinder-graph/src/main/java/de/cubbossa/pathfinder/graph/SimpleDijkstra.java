package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class SimpleDijkstra<N> implements PathSolver<N> {

  public Node buildGraph(Graph<N> graph, N source) {
    Map<N, Node> computed = new HashMap<>();
    Queue<Node> queue = new LinkedList<>();

    Node root = new Node(source, 0);
    queue.add(root);

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      computed.put(current.node, current);
      for (var entry : graph.getEdges(current.node).entrySet()) {
        Node adjacent = computed.computeIfAbsent(entry.getKey(), Node::new);
        if (!adjacent.settled) {
          queue.add(adjacent);
        }
        current.adjacent.put(adjacent, entry.getValue());
      }
      current.settled = true;
    }

    computed.values().forEach(node -> node.settled = false);
    return root;
  }

  private Map<N, Node> setStartNode(Graph<N> graph, N source) {
    Map<N, Node> computedGraph = new HashMap<>();
    Set<Node> unsettled = new HashSet<>();
    unsettled.add(buildGraph(graph, source));

    while (unsettled.size() > 0) {
      Node current = getNearest(unsettled);
      unsettled.remove(current);

      for (Map.Entry<Node, Double> entry : current.adjacent.entrySet()) {
        Node node = entry.getKey();
        if (!node.settled) {
          setMinDist(current, node, entry.getValue());
          unsettled.add(node);
        }
      }
      current.settled = true;
      computedGraph.put(current.node, current);
    }
    return computedGraph;
  }

  private Node getNearest(Set<Node> unsettled) {
    Node nearest = null;
    double dist = Integer.MAX_VALUE;
    for (Node node : unsettled) {
      if (node.distance < dist) {
        nearest = node;
        dist = node.distance;
      }
    }
    return nearest;
  }

  private void setMinDist(Node node, Node neighbour, double edgeWeigh) {
    double sourceDistance = node.distance;
    if (sourceDistance + edgeWeigh < neighbour.distance) {
      neighbour.distance = sourceDistance + edgeWeigh;
      LinkedList<Node> shortestPath = new LinkedList<>(node.path);
      shortestPath.add(node);
      neighbour.path = shortestPath;
    }
  }

  public List<N> solvePath(Graph<N> graph, N start, N target) throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkNotNull(target);

    Map<N, Node> computedGraph = setStartNode(graph, start);

    if (!computedGraph.containsKey(target)) {
      throw new NoPathFoundException();
    }
    List<N> result = computedGraph.get(target).path.stream().map(Node::getNode)
        .collect(Collectors.toCollection(LinkedList::new));
    result.add(target);
    return result;
  }

  public List<N> solvePath(Graph<N> graph, N start, Collection<N> targets)
      throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(start);
    Preconditions.checkState(targets.size() > 0);

    Map<N, Node> computedGraph = setStartNode(graph, start);

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
    List<N> path = nearest.path.stream()
        .map(Node::getNode)
        .collect(Collectors.toCollection(LinkedList::new));
    path.add(nearest.node);
    return path;
  }

  @Getter
  @RequiredArgsConstructor
  private class Node {
    private final N node;
    private final Map<Node, Double> adjacent = new HashMap<>();
    private boolean settled = false;
    private List<Node> path = new LinkedList<>();
    private double distance = Integer.MAX_VALUE;

    public Node(N node, double distance) {
      this.node = node;
      this.distance = distance;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }
  }
}
