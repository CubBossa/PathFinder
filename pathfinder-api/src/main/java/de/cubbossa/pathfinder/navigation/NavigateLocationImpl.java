package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;

class NavigateLocationImpl implements NavigationLocation {

  @Getter
  private final Node node;
  private final boolean fixed;
  private final boolean external;

  NavigateLocationImpl(Node node, boolean fixed, boolean external) {
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
  }

  @Override
  public boolean isFixedPosition() {
    return fixed;
  }
}
