package de.cubbossa.pathfinder.group

import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.misc.Keyed
import de.cubbossa.pathfinder.node.Node
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * A NodeGroup is the way to apply behaviour to a set of nodes.
 * <br></br>
 * NodeGroups extend [Modified] and the set of modifiers apply to all contained nodes.
 * If multiple groups apply to a node, the weight of the group decides its priority. The higher the weight,
 * the more important is the group. If two groups apply two conflicting modifiers to the same node, the modifier from
 * the group with the higher weight will be preferred.
 */
interface NodeGroup : Keyed, Modified, MutableSet<UUID?>, Comparable<NodeGroup?> {
    /**
     * If multiple groups apply to a node, the weight of the group decides its priority. The higher the weight,
     * * the more important is the group. If two groups apply two conflicting modifiers to the same node, the modifier from
     * * the group with the higher weight will be preferred.
     */
    var weight: Float

    /**
     * Turns the stored list of UUIDs into its according Node instances.
     * Internally, a storage access is necessary, therefore, the method returns a CompletableFuture.
     * Results might be cached.
     * @return The resolved collection of Node instances
     */
    fun resolve(): CompletableFuture<Collection<Node>>

    val contentChanges: Changes<UUID>

    val modifierChanges: Changes<Modifier>
}
