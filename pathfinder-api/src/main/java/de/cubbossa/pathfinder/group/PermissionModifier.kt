package de.cubbossa.pathfinder.group

import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString

interface PermissionModifier : Modifier {

    val permission: String

    companion object {
        val key: NamespacedKey = fromString("pathfinder:permission")
    }
}
