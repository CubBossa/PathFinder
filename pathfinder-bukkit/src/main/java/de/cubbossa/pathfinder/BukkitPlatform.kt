package de.cubbossa.pathfinder

import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.misc.pathConsoleSender
import de.cubbossa.pathfinder.misc.pathPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun Player.asPathPlayer(): PathPlayer<Player> = pathPlayer(this)

fun CommandSender.asPathPlayer(): PathPlayer<CommandSender> {
    return if (this is Player) {
        pathPlayer(this)
    } else {
        pathConsoleSender()
    }
}