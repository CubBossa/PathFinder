package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.util.CommandUtils
import de.cubbossa.pathfinder.group.DiscoverProgressModifier
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.util.CollectionUtils
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DiscoveriesCommand : CommandTree("discoveries") {
    private val pathFinder: PathFinder

    init {
        withPermission(PathPerms.PERM_CMD_DISCOVERIES)

        pathFinder = PathFinder.get()

        executesPlayer(PlayerCommandExecutor { sender, _ ->
            printList(sender, Pagination.page(0, 10))
        })
        then(
            Arguments.pagination(10)
                .executesPlayer(PlayerCommandExecutor { sender, args ->
                    printList(sender, args.getUnchecked(0))
                })
        )
    }

    private fun printList(sender: Player, pagination: Pagination?) {
        launchIO {
            val groups = pathFinder.storage.loadGroups(DiscoverProgressModifier.key)
            val l = groups.stream()
                .map { group -> group.getModifier<DiscoverProgressModifier>(DiscoverProgressModifier.key) }
                .parallel()
                .filter { it != null }.map { it as DiscoverProgressModifier }
                .map { modifier ->
                    java.util.Map.entry(
                        modifier,
                        runBlocking { modifier.calculateProgress(sender.uniqueId) }
                    )
                }
                .sorted(Comparator.comparingDouble { it.value })
                .toList()

            val p = BukkitUtils.wrap<CommandSender>(sender)
            CommandUtils.printList(
                sender, pagination,
                CollectionUtils.subList(l, pagination),
                { (mod, prog) ->
                    p.sendMessage(
                        Messages.CMD_DISCOVERIES_ENTRY.formatted(
                            Placeholder.component("name", mod.displayName),
                            Messages.formatter().number("percentage", prog * 100),
                            Messages.formatter().number("ratio", prog)
                        )
                    )
                },
                Messages.CMD_DISCOVERIES_HEADER,
                Messages.CMD_DISCOVERIES_FOOTER
            )
        }
    }
}
