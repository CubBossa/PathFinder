package de.cubbossa.pathfinder.node

import de.cubbossa.pathfinder.PathFinder
import lombok.Getter
import java.util.*

@Getter
class EdgeImpl(
    override val start: UUID,
    override val end: UUID,
    override val weight: Float
) : Edge {

    private val pathFinder: PathFinder = PathFinder.get()

    override suspend fun resolveStart(): Node? {
        return pathFinder.storage.loadNode(start)
    }

    override suspend fun resolveEnd(): Node? {
        return pathFinder.storage.loadNode(end)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EdgeImpl) {
            return false
        }

        if (other.weight.compareTo(weight) != 0) {
            return false
        }
        if (start != other.start) {
            return false
        }
        return end == other.end
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + (if (weight != 0.0f) java.lang.Float.floatToIntBits(
            weight
        ) else 0)
        return result
    }

    override fun toString(): String {
        return "Edge{" +
                "start=" + start +
                ", end=" + end +
                ", weightModifier=" + weight +
                '}'
    }
}
