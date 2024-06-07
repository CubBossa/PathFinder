package de.cubbossa.pathfinder.group

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString

interface FindDistanceModifier : Modifier {
    val distance: Double

    companion object {
        val key: NamespacedKey = fromString("pathfinder:find-distance")
    }
}
