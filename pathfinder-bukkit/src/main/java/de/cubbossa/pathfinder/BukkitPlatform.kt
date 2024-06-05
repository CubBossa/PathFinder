package de.cubbossa.pathfinder

import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.misc.pathConsoleSender
import de.cubbossa.pathfinder.misc.pathPlayer
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import net.kyori.adventure.text.ComponentLike
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector

fun Player.asPathPlayer(): PathPlayer<Player> = pathPlayer(this)

fun CommandSender.asPathPlayer(): PathPlayer<CommandSender> {
    return if (this is Player) {
        pathPlayer(this)
    } else {
        pathConsoleSender()
    }
}

fun CommandSender.sendMessage(component: ComponentLike) {
    this.asPathPlayer().sendMessage(component)
}

fun NamespacedKey.toPathFinder(): de.cubbossa.pathfinder.misc.NamespacedKey {
    return BukkitPathFinder.convert(this)
}

fun de.cubbossa.pathfinder.misc.NamespacedKey.toBukkit(): NamespacedKey {
    return NamespacedKey(this.namespace, this.key)
}

fun Vector.toPathFinder(): de.cubbossa.pathfinder.misc.Vector {
    return BukkitVectorUtils.toInternal(this)
}

fun de.cubbossa.pathfinder.misc.Vector.toPathFinder(): Vector {
    return BukkitVectorUtils.toBukkit(this)
}

fun Location.toPathFinder(): de.cubbossa.pathfinder.misc.Location {
    return BukkitVectorUtils.toInternal(this)
}

fun de.cubbossa.pathfinder.misc.Location.toPathFinder(): Location {
    return BukkitVectorUtils.toBukkit(this)
}