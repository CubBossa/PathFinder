package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SimpleAStar<N> implements PathSolver<N> {

  private final BiFunction<N, N, Double> distanceFunction;

  public SimpleAStar(BiFunction<N, N, Double> distanceFunction) {
    this.distanceFunction = distanceFunction;
  }

  public List<N> solvePath(Graph<N> graph, N startNode, N targetNode) throws NoPathFoundException {
    return solvePath(graph, startNode, List.of(targetNode));
  }

  @Override
  public List<N> solvePath(Graph<N> graph, N startNode, Collection<N> targets)
      throws NoPathFoundException {
    Preconditions.checkNotNull(graph);
    Preconditions.checkNotNull(startNode);
    Preconditions.checkState(targets.size() > 0);

    Map<N, Node> nodeMap = buildGraph(graph, startNode, targets);

    Node start = nodeMap.get(startNode);

    return shortestPath(
        start,
        targets.stream()
            .map(nodeMap::get)
            .collect(Collectors.toList())
    )
        .stream()
        .map(Node::getNode)
        .collect(Collectors.toList());
  }

  private List<Node> shortestPath(Node start, Collection<Node> target)
      throws NoPathFoundException {

    if (target.isEmpty()) {
      throw new NoPathFoundException();
    }

    TreeSet<Node> open = new TreeSet<>();
    Collection<Node> close = new HashSet<>();
    Node matchedTarget = null;

    open.add(start);

    Node current;
    while (!open.isEmpty()) {

      current = open.pollFirst();

      if (target.contains(current)) {
        matchedTarget = current;
        break;
      }

      for (Map.Entry<Node, Double> entry : current.adjacent.entrySet()) {
        Node successor = entry.getKey();

        if (close.contains(successor)) {
          continue;
        }

        double cost = current.g + entry.getValue() * distanceFunction
            .apply(current.node, successor.node);

        if (open.contains(successor)) {
          if (cost >= successor.g) {
            continue;
          }
        }
        successor.predecessor = current;
        successor.g = cost;
        successor.f = cost + successor.h;

        open.add(successor);
      }
      close.add(current);
    }

    if (matchedTarget == null) {
      throw new NoPathFoundException();
    }

    List<Node> path = new ArrayList<>();
    Node c = matchedTarget;
    while (c != null) {
      path.add(0, c);
      c = c.predecessor;
    }
    return path;
  }

  private Map<N, Node> buildGraph(Graph<N> graph, N start, Collection<N> targets) {
    Map<N, Node> computed = new HashMap<>();
    Queue<Node> queue = new LinkedList<>();

    queue.add(new Node(start));

    while (!queue.isEmpty()) {
      Node current = queue.poll();

      current.h = targets.stream()
          .mapToDouble(t -> distanceFunction.apply(current.node, t))
          .min().orElse(Double.MAX_VALUE);
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
    return computed;
  }

  @Getter
  private class Node implements Comparable<Node> {
    private final N node;
    private Node predecessor;

    // total cost
    private double f;
    // distance between current node and start node
    private double g;
    // estimated distance from current to target
    private double h;


    private boolean settled = false;
    private final Map<Node, Double> adjacent = new HashMap<>();
    private final List<Node> path = new LinkedList<>();

    public Node(N node) {
      this.node = node;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }

    @Override
    public int compareTo(@NotNull SimpleAStar<N>.Node o) {
      return Double.compare(this.f, o.f);
    }
  }
}
