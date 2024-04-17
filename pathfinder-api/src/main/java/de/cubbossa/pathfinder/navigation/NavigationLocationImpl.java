package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathfinder.misc.GraphEntrySolver;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;

public class NavigationLocationImpl implements NavigationLocation {

  public static GraphEntrySolver<Node> GRAPH_ENTRY_SOLVER;

  @Getter
  private final Node node;
  private final boolean fixed;
  private final boolean external;

  NavigationLocationImpl(Node node, boolean fixed, boolean external) {
    this.node = node;
    this.fixed = fixed;
    this.external = external;
  }

  @Override
  public void connect(MutableValueGraph<Node, Double> graph) {
    if (!external) {
      return;
    }
    if (graph.nodes().contains(node)) {
      return;
    }
    GRAPH_ENTRY_SOLVER.solveEntry(node, graph);
    GRAPH_ENTRY_SOLVER.solveExit(node, graph);
  }

  @Override
  public boolean isFixedPosition() {
    return fixed;
  }
}
