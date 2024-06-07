package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.paginationArgument
import de.cubbossa.pathfinder.command.util.CommandUtils
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender

class ListGroupsCmd(pathFinder: PathFinder?) : PathFinderSubCommand(
    pathFinder!!, "listgroups"
) {
    init {
        withPermission(PathPerms.PERM_CMD_NG_LIST)
        executes(CommandExecutor { sender: CommandSender, _ ->
            listGroups(sender, Pagination.page(0, 10))
        })
        paginationArgument(10) {
            executes(CommandExecutor { sender: CommandSender, args: CommandArguments ->
                listGroups(sender, args.getUnchecked(0)!!)
            })
        }
    }

    private fun listGroups(sender: CommandSender, pagination: Pagination) = launchIO {
        val nodeGroups = pathfinder.storage.loadGroups(pagination)
        CommandUtils.printList(
            sender,
            pagination,
            ArrayList(nodeGroups),
            {
                BukkitUtils.wrap(sender).sendMessage(
                    Messages.CMD_NG_LIST_LINE.formatted(
                        Messages.formatter().namespacedKey("key", it.key),
                        Messages.formatter().number("size", it.size),
                        Messages.formatter().number("weight", it.weight),
                        Messages.formatter().modifiers("modifiers", it.modifiers)
                    )
                )
            },
            Messages.CMD_NG_LIST_HEADER,
            Messages.CMD_NG_LIST_FOOTER
        )
    }
}
