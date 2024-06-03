package de.cubbossa.pathfinder.misc

/**
 * A [Named] that can also be renamed.
 */
interface Nameable : Named {

    /**
     * Sets the display name. The call if this method must also refresh the display name component if stored locally.
     *
     * @param name The display name formatted in the MiniMessage style.
     * @see "https://docs.adventure.kyori.net/minimessage/format.html"
     */
    override var nameFormat: String
}
