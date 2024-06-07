package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.AbstractPathFinder.Companion.globalGroupKey
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.nodeGroupArgument
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender

class DeleteGroupCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "deletegroup") {
    init {
        withGeneratedHelp()
        withPermission(PathPerms.PERM_CMD_NG_DELETE)
        nodeGroupArgument("group") {
            executes(CommandExecutor { sender: CommandSender, args: CommandArguments ->
                deleteGroup(sender, args.getUnchecked(0)!!)
            })
        }
    }

    private fun deleteGroup(sender: CommandSender, group: NodeGroup) {
        val p: PathPlayer<*> = BukkitUtils.wrap(sender)
        if (group.key == globalGroupKey()) {
            p.sendMessage(Messages.CMD_NG_DELETE_GLOBAL)
            return
        }
        launchIO {
            pathfinder.storage.deleteGroup(group)
            p.sendMessage(
                Messages.CMD_NG_DELETE.formatted(
                    Messages.formatter().namespacedKey("key", group.key)
                )
            )
        }
    }
}
