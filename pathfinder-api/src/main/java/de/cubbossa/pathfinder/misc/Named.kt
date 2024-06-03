package de.cubbossa.pathfinder.misc

import net.kyori.adventure.text.Component

/**
 * An object that can be represented as Component.
 */
interface Named {
    /**
     * @return The display name formatted in the MiniMessage style.
     * @see "https://docs.adventure.kyori.net/minimessage/format.html"
     */
    val nameFormat: String

    /**
     * @return The display name as kyori Component. Must be the parsed version of the name format string and
     * may be cached.
     */
    val displayName: Component
}
