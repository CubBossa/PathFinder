package de.cubbossa.pathfinder

import de.cubbossa.pathfinder.misc.*
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLoc
import org.bukkit.NamespacedKey as BukkitNamespacedKey
import org.bukkit.util.Vector as BukkitVec

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

fun BukkitNamespacedKey.toPathFinder(): NamespacedKey {
    return BukkitPathFinder.convert(this)
}

fun NamespacedKey.toBukkit(): BukkitNamespacedKey {
    return BukkitNamespacedKey(this.namespace, this.key)
}

fun BukkitVec.toPathFinder(): Vector {
    return BukkitVectorUtils.toInternal(this)
}

fun Vector.toPathFinder(): BukkitVec {
    return BukkitVectorUtils.toBukkit(this)
}

fun BukkitLoc.toPathFinder(): Location {
    return BukkitVectorUtils.toInternal(this)
}

fun Location.toPathFinder(): BukkitLoc {
    return BukkitVectorUtils.toBukkit(this)
}