package de.cubbossa.pathfinder.navigation

import de.cubbossa.pathfinder.node.Node
import java.util.*

/**
 * Navigation filters allow to filter the required targets of the navigation.
 * This might be used to filter nodes that have certain [de.cubbossa.pathfinder.group.Modifier]s applied.
 * If the result is empty, navigation will be cancelled.
 */
fun interface NavigationConstraint {
    /**
     * Filters the requested target locations, for some might not be supposed to navigate to in certain contexts.
     * @param playerId The UUID of the player that requested the navigation.
     * @param scope The target nodes the player requests to navigate to.
     * @return The filtered scope that has been provided.
     */
    fun filterTargetNodes(playerId: UUID, scope: Collection<Node>): Collection<Node>
}
