package de.cubbossa.pathfinder.node.implementation

import de.cubbossa.pathfinder.Changes
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.EdgeImpl
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.ModifiedHashSet
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
class Waypoint(
    override val nodeId: UUID,
    override var location: Location
) : Node, Cloneable {

    override val edges: ModifiedHashSet<Edge> = ModifiedHashSet()
    private val groups: MutableCollection<NodeGroup> = HashSet()

    override val edgeChanges: Changes<Edge>
        get() = edges.changes

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Node) {
            return false
        }
        return nodeId == other.nodeId
    }

    override fun hashCode(): Int {
        return nodeId.hashCode()
    }

    fun getGroups(): Collection<NodeGroup> {
        return HashSet(groups)
    }

    override fun toString(): String {
        return "Waypoint{" +
                "nodeId=" + nodeId +
                ", location=" + location +
                '}'
    }

    override fun connect(other: UUID, weight: Double): Edge? {
        if (getConnection(other) == null) {
            return null
        }
        val e: Edge = EdgeImpl(nodeId, other, weight.toFloat())
        edges.add(e)
        return e
    }

    override fun clone(id: UUID): Waypoint {
        val clone = Waypoint(id, location)
        clone.edges.addAll(this.edges)
        clone.groups.addAll(groups)
        return clone
    }

    override fun clone(): Waypoint {
        return try {
            super<Cloneable>.clone() as Waypoint
        } catch (e: CloneNotSupportedException) {
            clone(nodeId)
        }
    }
}
