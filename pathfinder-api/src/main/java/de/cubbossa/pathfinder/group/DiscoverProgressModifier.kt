package de.cubbossa.pathfinder.group

import de.cubbossa.pathfinder.misc.Named
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString
import java.util.*

interface DiscoverProgressModifier : Modifier, Named {

    val owningGroup: NamespacedKey

    suspend fun calculateProgress(playerId: UUID): Double

    companion object {
        val key: NamespacedKey = fromString("pathfinder:discover-progress")
    }
}
