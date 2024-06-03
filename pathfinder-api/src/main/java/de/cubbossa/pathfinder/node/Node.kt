package de.cubbossa.pathfinder.node

import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.misc.Location
import java.util.*

/**
 * A node instance is the main structure of the virtual graph that is managed by PathFinder.
 * They are persistent data of the application that the user specifies to defines possible paths.
 * Actual pathfinding will happen on a graph built conditionally from all nodes.
 *
 * @see Edge serves as connecting structure.
 */
@JvmDefaultWithCompatibility
interface Node : Comparable<Node>, Cloneable {

    /**
     * The UUID of nodes must be unique for each node and serves as primary key.
     *
     * @return The UUID of this Node.
     */
    val nodeId: UUID

    /**
     * The current location of this Node. A location consists of a vec3 and a world. Worlds are abstract
     * and must not be minecraft worlds, bust most commonly are. For example, a world could also resemble a website.
     *
     * @return A referenced location of this Node.
     */
    var location: Location

    val edgeChanges: Changes<Edge>

    val edges: Collection<Edge>

    fun connect(other: Node): Edge? {
        return connect(other.nodeId)
    }

    fun connect(other: UUID): Edge? {
        return connect(other, 1.0)
    }

    fun connect(other: Node, weight: Double): Edge? {
        return connect(other.nodeId, weight)
    }

    fun connect(other: UUID, weight: Double): Edge?

    fun disconnectAll() {
        (edges as? MutableCollection)?.clear()
    }

    fun disconnect(other: Node): Edge? {
        return disconnect(other.nodeId)
    }

    fun disconnect(other: UUID): Edge? {
        val opt = edges.stream().filter { edge: Edge -> edge.end == other }
            .findAny()
        opt.ifPresent { edge: Edge -> (edges as? MutableCollection)?.remove(edge) }
        return opt.orElse(null)
    }

    fun hasConnection(other: Node): Boolean {
        return hasConnection(other.nodeId)
    }

    fun hasConnection(other: UUID): Boolean {
        return edges.stream()
            .map { obj: Edge -> obj.end }
            .anyMatch { obj: UUID? -> other.equals(obj) }
    }

    fun getConnection(other: Node): Edge? {
        return getConnection(other.nodeId)
    }

    fun getConnection(other: UUID): Edge? {
        return edges.stream().filter { edge: Edge -> edge.end == other }
            .findAny().orElse(null)
    }


    override fun compareTo(other: Node): Int {
        return nodeId.compareTo(other.nodeId)
    }

    public override fun clone(): Node

    fun clone(id: UUID): Node
}
