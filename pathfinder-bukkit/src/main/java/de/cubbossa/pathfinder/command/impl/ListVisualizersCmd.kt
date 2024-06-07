package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.paginationArgument
import de.cubbossa.pathfinder.command.util.CommandUtils
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Keyed
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.sendMessage
import de.cubbossa.pathfinder.util.CollectionUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import java.util.stream.Collectors

class ListVisualizersCmd(pathFinder: PathFinder) :
    PathFinderSubCommand(pathFinder, "listvisualizers") {
    init {
        withPermission(PathPerms.PERM_CMD_PV_LIST)
        executes(CommandExecutor { commandSender: CommandSender, objects: CommandArguments? ->
            onList(commandSender, Pagination.page(0, 10))
        })
        paginationArgument(10) {
            executes(CommandExecutor { commandSender: CommandSender, objects: CommandArguments ->
                onList(commandSender, objects.getUnchecked(0)!!)
            })
        }
    }

    private fun onList(sender: CommandSender, pagination: Pagination) = launchIO {
        val pathVisualizers = pathfinder.storage.loadVisualizers()
        val map = pathfinder.storage.loadVisualizerTypes(
            pathVisualizers.stream()
                .map(Keyed::key).collect(Collectors.toList())
        )

        //TODO pagination in load
        CommandUtils.printList(
            sender, pagination,
            CollectionUtils.subList(ArrayList(pathVisualizers), pagination),
            {
                val r = TagResolver.builder()
                    .resolver(
                        Messages.formatter().namespacedKey("key", it.key)
                    )
                    .resolver(
                        Messages.formatter().namespacedKey("type", map[it.key]?.key)
                    )
                    .build()
                sender.sendMessage(Messages.CMD_VIS_LIST_ENTRY.formatted(r))
            },
            Messages.CMD_VIS_LIST_HEADER,
            Messages.CMD_VIS_LIST_FOOTER
        )
    }
}
