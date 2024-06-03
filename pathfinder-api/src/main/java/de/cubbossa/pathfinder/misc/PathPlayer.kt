package de.cubbossa.pathfinder.misc

import de.cubbossa.disposables.Disposable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import java.util.*

fun <P> pathPlayer(player: P): PathPlayer<P> {
    return PathPlayerProvider.get<P>().wrap(player) as PathPlayer<P>
}

fun <P> pathPlayer(uuid: UUID?): PathPlayer<P> {
    return PathPlayerProvider.get<P>().wrap(uuid) as PathPlayer<P>
}

fun <P> pathConsoleSender(): PathPlayer<P> {
    return PathPlayerProvider.get<P>().consoleSender() as PathPlayer<P>
}

interface PathPlayer<P> : Disposable {

    val uniqueId: UUID
    val playerClass: Class<P>
    val name: String?
    val displayName: Component?
    val location: Location

    fun hasPermission(permission: String): Boolean

    fun unwrap(): P

    fun sendMessage(message: ComponentLike)
}
