package de.cubbossa.pathfinder.navigation;

import de.cubbossa.pathfinder.node.Node;

public interface NavigationLocation {

  static NavigationLocation fixedGraphNode(Node node) {
    return new NavigationLocationImpl(node, true, false);
  }

  static NavigationLocation fixedExternalNode(Node node) {
    return new NavigationLocationImpl(node, true, true);
  }

  static NavigationLocation movingGraphNode(Node node) {
    return new NavigationLocationImpl(node, false, false);
  }

  static NavigationLocation movingExternalNode(Node node) {
    return new NavigationLocationImpl(node, false, true);
  }

  Node getNode();

  void setNode(Node node);

  boolean isFixedPosition();

  boolean isExternal();
}
