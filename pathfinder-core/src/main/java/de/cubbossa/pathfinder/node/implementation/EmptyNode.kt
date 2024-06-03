package de.cubbossa.pathfinder.node.implementation

import de.cubbossa.pathfinder.AbstractPathFinder
import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.World
import de.cubbossa.pathfinder.node.AbstractNodeType
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import java.util.*

class EmptyNode(
    override val nodeId: UUID,
    world: World
) : Node {

    override var location: Location = Location(0.0, 0.0, 0.0, world)
        set(value) {}

    override val edgeChanges: Changes<Edge> = Changes()
    override val edges: MutableCollection<Edge>
        get() = HashSet()

    constructor(world: World) : this(UUID.randomUUID(), world)

    override fun connect(other: UUID, weight: Double): Edge? {
        return null
    }

    override fun compareTo(other: Node): Int {
        return 0
    }

    override fun clone(id: UUID): Node {
        return EmptyNode(nodeId, location.world)
    }

    override fun clone(): Node {
        return clone(nodeId)
    }

    override fun toString(): String {
        return "EmptyNode{}"
    }

    companion object {
        val TYPE: NodeType<EmptyNode> = object : AbstractNodeType<EmptyNode>(
            AbstractPathFinder.pathfinder("empty")
        ) {
            override fun createNodeInstance(context: NodeType.Context): EmptyNode {
                return EmptyNode(context.id, context.location.world)
            }

            override fun canBeCreated(context: NodeType.Context): Boolean {
                return false
            }

            override fun createAndLoadNode(context: NodeType.Context): EmptyNode? {
                throw IllegalStateException(
                    "EmptyNode are only part of runtime navigation and "
                            + "must be created from constructor."
                )
            }
        }
    }
}
