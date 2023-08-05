package de.cubbossa.pathfinder.graph;

import com.google.common.base.Preconditions;
import com.google.common.graph.ValueGraph;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AStarImpl<N> implements PathSolver<N> {

  private final BiFunction<N, N, Double> distanceFunction;
  private ValueGraph<N, Double> graph;

  public AStarImpl(BiFunction<N, N, Double> distanceFunction) {
    this.distanceFunction = distanceFunction;
  }

  @Override
  public void setGraph(ValueGraph<N, Double> graph) {
    this.graph = graph;
  }

  @Override
  public List<N> solvePath(N startNode, Collection<N> targets)
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

  private Map<N, Node> buildGraph(ValueGraph<N, Double> graph, N start, Collection<N> targets) {
    Map<N, Node> computed = new HashMap<>();
    Queue<Node> queue = new LinkedList<>();

    queue.add(new Node(start));

    while (!queue.isEmpty()) {
      Node current = queue.poll();

      current.h = targets.stream()
          .mapToDouble(t -> distanceFunction.apply(current.node, t))
          .min().orElse(Double.MAX_VALUE);
      computed.put(current.node, current);

      for (var entry : graph.successors(current.node)) {
        Node adjacent = computed.computeIfAbsent(entry, Node::new);
        if (!adjacent.settled) {
          queue.add(adjacent);
        }
        current.adjacent.put(adjacent, graph.edgeValue(current.node, entry).orElseThrow());
      }
      current.settled = true;
    }

    computed.values().forEach(node -> node.settled = false);
    return computed;
  }

  @Getter
  private class Node implements Comparable<Node> {
    private final N node;
    private final Map<Node, Double> adjacent = new HashMap<>();
    private final List<Node> path = new LinkedList<>();
    private Node predecessor;
    // total cost
    private double f;
    // distance between current node and start node
    private double g;
    // estimated distance from current to target
    private double h;
    private boolean settled = false;

    public Node(N node) {
      this.node = node;
    }

    @Override
    public int hashCode() {
      return node.hashCode();
    }

    @Override
    public int compareTo(@NotNull AStarImpl<N>.Node o) {
      return Double.compare(this.f, o.f);
    }
  }
}
