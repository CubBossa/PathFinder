package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import de.cubbossa.pathfinder.node.Node;

public interface NavigationLocation {

  static NavigationLocation fixedGraphNode(Node node) {
    return new NavigateLocationImpl(node, true, false);
  }

  static NavigationLocation fixedExternalNode(Node node) {
    return new NavigateLocationImpl(node, true, true);
  }

  static NavigationLocation movingGraphNode(Node node) {
    return new NavigateLocationImpl(node, false, false);
  }

  static NavigationLocation movingExternalNode(Node node) {
    return new NavigateLocationImpl(node, false, true);
  }

  Node getNode();

  void connect(MutableValueGraph<Node, Double> graph);

  boolean isFixedPosition();
}
