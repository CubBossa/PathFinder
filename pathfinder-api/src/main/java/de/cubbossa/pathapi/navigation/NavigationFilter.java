package de.cubbossa.pathapi.navigation;

import de.cubbossa.pathapi.node.Node;
import java.util.Collection;
import java.util.UUID;

/**
 * Navigation filters allow to filter the required targets of the navigation.
 * This might be used to filter nodes that have certain {@link de.cubbossa.pathapi.group.Modifier}s applied.
 * If the result is empty, navigation will be cancelled.
 */
public interface NavigationFilter {

  /**
   * Filters the requested target locations, for some might not be supposed to navigate to in certain contexts.
   * @param playerId The UUID of the player that requested the navigation.
   * @param scope The target nodes the player requests to navigate to.
   * @return The filtered scope that has been provided.
   */
  Collection<Node> filterTargetNodes(UUID playerId, Collection<Node> scope);
}
