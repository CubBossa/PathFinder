package de.cubbossa.pathfinder.storage

import de.cubbossa.pathfinder.node.implementation.Waypoint
import java.util.*

interface WaypointStorageImplementation {

    fun loadWaypoint(uuid: UUID): Waypoint?

    fun loadWaypoints(ids: Collection<UUID>): Collection<Waypoint>

    fun loadAllWaypoints(): Collection<Waypoint>

    fun saveWaypoint(node: Waypoint)

    fun deleteWaypoints(waypoints: Collection<Waypoint>)
}
