package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.module.visualizing.query.SearchTermHolder;
import java.util.Collection;

public interface Navigable extends SearchTermHolder {

  /**
   * @return The group of all nodes that belong to this navigable.
   * This might be just one node for single nodes that implement navigable.
   * Group content for groups.
   */
  Collection<Node> getGroup();
}
