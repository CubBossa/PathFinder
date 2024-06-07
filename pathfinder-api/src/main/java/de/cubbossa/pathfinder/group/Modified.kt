package de.cubbossa.pathfinder.group

import de.cubbossa.pathfinder.misc.NamespacedKey

interface Modified {
    val modifiers: Collection<Modifier>

    fun <M : Modifier> hasModifier(modifierClass: Class<M>): Boolean

    fun <M : Modifier> hasModifier(modifierType: NamespacedKey): Boolean

    fun <M : Modifier> addModifier(modifier: M) {
        addModifier(modifier.key, modifier)
    }

    fun addModifier(key: NamespacedKey, modifier: Modifier)

    fun <M : Modifier> getModifier(key: NamespacedKey): M?

    fun <M : Modifier> removeModifier(modifierClass: Class<M>)

    fun <M : Modifier> removeModifier(modifier: M)

    fun removeModifier(key: NamespacedKey)

    fun clearModifiers()
}

inline fun <reified M : Modifier> Modified.hasModifier(): Boolean {
    return hasModifier(M::class.java)
}
