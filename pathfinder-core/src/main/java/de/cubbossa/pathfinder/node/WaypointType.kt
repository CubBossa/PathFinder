package de.cubbossa.pathfinder.node

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString
import de.cubbossa.pathfinder.node.implementation.Waypoint
import de.cubbossa.pathfinder.storage.implementation.NodeStorageImplementationWrapper
import org.pf4j.Extension

@Extension(points = [NodeType::class])
class WaypointType : AbstractNodeType<Waypoint>(
    fromString("pathfinder:waypoint"),
    NodeStorageImplementationWrapper(PathFinder.get().storage)
) {

    override fun createNodeInstance(context: NodeType.Context): Waypoint {
        return Waypoint(context.id, context.location)
    }
}
