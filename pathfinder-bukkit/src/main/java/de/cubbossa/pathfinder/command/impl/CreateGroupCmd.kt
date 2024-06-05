package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.AbstractPathFinder.pathfinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import java.util.*
import java.util.logging.Level

class CreateGroupCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "creategroup") {
    init {
        withGeneratedHelp()

        withPermission(PathPerms.PERM_CMD_NG_CREATE)

        then(stringArgument("name") {
            anyExecutor { sender: CommandSender, args: CommandArguments ->
                createGroup(
                    sender,
                    args.getUnchecked<Any>(0).toString().lowercase(
                        Locale.getDefault()
                    )
                )
            }
        })
    }

    private fun createGroup(sender: CommandSender, name: String) = launchIO {
        val key = pathfinder(name)
        val group = pathfinder.storage.loadGroup(key)
        if (group != null) {
            BukkitUtils.wrap(sender).sendMessage(
                Messages.CMD_NG_ALREADY_EXISTS.formatted(Placeholder.parsed("key", name))
            )
        }
        try {
            val newGroup = pathfinder.storage.createAndLoadGroup(pathfinder(name))
            BukkitUtils.wrap(sender).sendMessage(
                Messages.CMD_NG_CREATE.formatted(
                    Messages.formatter().namespacedKey("key", newGroup.key)
                )
            )
        } catch (t: Throwable) {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_CREATE_FAIL)
            pathfinder.logger.log(Level.SEVERE, "Could not create nodegroup.", t)
        }
    }
}
