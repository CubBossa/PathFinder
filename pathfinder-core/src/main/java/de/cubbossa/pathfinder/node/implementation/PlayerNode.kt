package de.cubbossa.pathfinder.node.implementation

import de.cubbossa.pathfinder.AbstractPathFinder
import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.AbstractNodeType
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import lombok.Getter
import java.util.*

@Getter
class PlayerNode(private val player: PathPlayer<*>) : Node, Cloneable {
    override val edgeChanges: Changes<Edge> = Changes()

    override val nodeId: UUID
        get() = player.uniqueId

    override var location: Location
        get() = player.location
        set(location) {
        }

    override val edges: MutableCollection<Edge>
        get() = HashSet()

    override fun connect(other: UUID, weight: Double): Edge? {
        return null
    }

    override fun compareTo(o: Node): Int {
        return 0
    }

    override fun clone(): Node {
        return PlayerNode(player)
    }

    override fun clone(id: UUID): Node {
        throw IllegalStateException("Cannot clone a player node with ID parameter.")
    }

    override fun toString(): String {
        return "PlayerNode{" +
                "player=" + player.name +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) {
            if (other is Node) {
                return other.nodeId == nodeId
            }
            return false
        }
        val that = other as PlayerNode
        return player.uniqueId == that.player.uniqueId
    }

    override fun hashCode(): Int {
        return player.uniqueId.hashCode()
    }

    companion object {
        val TYPE: NodeType<PlayerNode> = object : AbstractNodeType<PlayerNode>(
            AbstractPathFinder.pathfinder("player")
        ) {
            override fun canBeCreated(context: NodeType.Context): Boolean {
                return false
            }

            override fun createNodeInstance(context: NodeType.Context): PlayerNode {
                throw IllegalStateException(
                    "PlayerNodes are only part of runtime navigation and "
                            + "must be created from constructor."
                )
            }

            override fun createAndLoadNode(context: NodeType.Context): PlayerNode? {
                throw IllegalStateException(
                    "PlayerNodes are only part of runtime navigation and "
                            + "must be created from constructor."
                )
            }
        }
    }
}
