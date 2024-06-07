package de.cubbossa.pathfinder.nodegroup.modifier

import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.PermissionModifier
import de.cubbossa.pathfinder.misc.NamespacedKey

class PermissionModifierImpl(
    override val permission: String
) : PermissionModifier {

    override val key: NamespacedKey
        get() = PermissionModifier.key

    override fun equals(other: Any?): Boolean {
        return other !is Modifier || key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
