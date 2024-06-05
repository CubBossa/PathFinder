package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.asPathPlayer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.navigation.NavigationModule
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player

class CancelPathCommand : CommandTree("cancelpath") {

    init {
        val module: NavigationModule<Player> = NavigationModule.get();

        withPermission(PathPerms.PERM_CMD_CANCELPATH)
        withRequirement { sender -> sender is Player && module.getActivePath(sender.asPathPlayer()) != null }

        executesPlayer(PlayerCommandExecutor { player, args ->
            module.cancel(player.uniqueId);
        })
    }

    fun refresh(player: PathPlayer<Player>) {
        CommandAPI.updateRequirements(player.unwrap());
    }
}