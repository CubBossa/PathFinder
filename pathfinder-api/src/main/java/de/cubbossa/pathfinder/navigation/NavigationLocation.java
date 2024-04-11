package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.ValueGraph;
import de.cubbossa.pathfinder.node.Node;

public interface NavigationLocation {

  Node getNode();

  void connect(ValueGraph<Node, Double> graph);

  boolean isFixedPosition();
}
