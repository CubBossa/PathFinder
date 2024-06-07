package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.nodeSelectionArgument
import de.cubbossa.pathfinder.command.paginationArgument
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.node.NodeSelection
import de.cubbossa.pathfinder.node.NodeSelectionImpl
import de.cubbossa.pathfinder.util.NodeUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player

class ListNodesCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "listnodes") {

    init {
        withPermission(PathPerms.PERM_CMD_WP_LIST)
        executesPlayer(PlayerCommandExecutor { sender: Player?, _ ->
            val selection = NodeSelection.ofSender("@n", sender!!)
            NodeUtils.onList(sender, NodeSelectionImpl(selection), Pagination.page(0, 10))
        })
        nodeSelectionArgument("nodes") {
            executesPlayer(PlayerCommandExecutor { player: Player?, args: CommandArguments ->
                NodeUtils.onList(player, args.getUnchecked(0), Pagination.page(0, 10))
            })
            paginationArgument(10) {
                executesPlayer(PlayerCommandExecutor { player: Player?, args: CommandArguments ->
                    NodeUtils.onList(player, args.getUnchecked(0), args.getUnchecked(1))
                })
            }
        }
    }
}