package de.cubbossa.pathfinder.navigation;

import com.google.common.base.Preconditions;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;

public class NavigationLocationImpl implements NavigationLocation {

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
  public void setNode(Node node) {
    this.node = node;
  }

  @Override
  public boolean isFixedPosition() {
    return fixed;
  }

  @Override
  public boolean isExternal() {
    return external;
  }

  @Override
  public String toString() {
    return "NavigationLocation{node=" + node.toString() + "}";
  }
}
