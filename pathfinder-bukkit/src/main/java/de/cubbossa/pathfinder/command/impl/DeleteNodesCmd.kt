package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.nodeSelectionArgument
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.node.NodeSelectionImpl
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeleteNodesCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "deletenodes") {
    init {
        withPermission(PathPerms.PERM_CMD_WP_DELETE)
        nodeSelectionArgument("nodes") {
            executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                deleteNode(player, args.getUnchecked(0)!!)
            })
        }
    }

    private fun deleteNode(sender: CommandSender, nodes: NodeSelectionImpl) {
        launchIO {
            pathfinder.storage.deleteNodes(nodes.ids)
            BukkitUtils.wrap(sender).sendMessage(
                Messages.CMD_N_DELETE.formatted(
                    Messages.formatter().nodeSelection("selection") { nodes }
                )
            )
        }
    }
}
