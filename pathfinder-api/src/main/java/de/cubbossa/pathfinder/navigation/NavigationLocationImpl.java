package de.cubbossa.pathfinder.navigation;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
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
  public MutableValueGraph<Node, Double> connect(MutableValueGraph<Node, Double> graph) {
    if (!external) {
      return graph;
    }
    MutableValueGraph<Node, Double> g = GRAPH_ENTRY_SOLVER.solve(node, graph);
    this.node = g.nodes().stream()
        .filter(n -> n.getNodeId().equals(node.getNodeId())).findAny()
        .orElseThrow();
    return g;
  }

  @Override
  public boolean isFixedPosition() {
    return fixed;
  }
}
