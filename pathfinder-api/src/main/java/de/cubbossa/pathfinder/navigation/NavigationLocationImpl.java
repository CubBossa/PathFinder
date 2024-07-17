package de.cubbossa.pathfinder.navigation;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.graph.GraphUtils;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;

public class NavigationLocationImpl implements NavigationLocation {

  public static GraphEntrySolver<Node> GRAPH_ENTRY_SOLVER;

  @Getter
  private Node node;
  private final boolean fixed;
  private final boolean external;

  NavigationLocationImpl(Node node, boolean fixed, boolean external) {
    Preconditions.checkNotNull(node);

    this.node = node;
    this.fixed = fixed;
    this.external = external;
  }

  @Override
  public ValueGraph<Node, Double> connect(ValueGraph<Node, Double> graph) {
    return connect(graph, true, true);
  }

  @Override
  public ValueGraph<Node, Double> connectAsEntry(ValueGraph<Node, Double> graph) {
    return connect(graph, true, false);
  }

  @Override
  public ValueGraph<Node, Double> connectAsExit(ValueGraph<Node, Double> graph) {
    return connect(graph, false, true);
  }

  public ValueGraph<Node, Double> connect(ValueGraph<Node, Double> graph, boolean entry, boolean exit) {
    Preconditions.checkArgument(entry || exit, "Either entry or exit must be true.");
    if (!external) {
      return graph;
    }
    MutableValueGraph<Node, Double> g = GRAPH_ENTRY_SOLVER.solve(node, GraphUtils.mutable(graph));
    this.node = g.nodes().stream()
        .filter(n -> n.getNodeId().equals(node.getNodeId())).findAny()
        .orElseThrow();
    return g;
  }

  @Override
  public boolean isFixedPosition() {
    return fixed;
  }

  @Override
  public String toString() {
    return "NavigationLocation{node=" + node.toString() + "}";
  }
}
