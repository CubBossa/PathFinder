package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.ModifierCommandExtension
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.literalArgument
import de.cubbossa.pathfinder.command.nodeGroupArgument
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.nodegroup.NodeGroupImpl
import de.cubbossa.pathfinder.sendMessage
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * A command to manage NodeGroups.
 */
class GroupCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "group") {
    /**
     * A command to manage NodeGroups.
     */
    init {
        withGeneratedHelp()

        withRequirement { sender: CommandSender ->
            (sender.hasPermission(PathPerms.PERM_CMD_NG_LIST)
                    || sender.hasPermission(PathPerms.PERM_CMD_NG_INFO)
                    || sender.hasPermission(PathPerms.PERM_CMD_NG_CREATE)
                    || sender.hasPermission(PathPerms.PERM_CMD_NG_DELETE)
                    || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_MOD)
                    || sender.hasPermission(PathPerms.PERM_CMD_NG_UNSET_MOD))
        }

        var set: Argument<*> = literalArgument("set") {
            withPermission(PathPerms.PERM_CMD_NG_SET_MOD)
        }
        val unset: Argument<*> = literalArgument("unset") {
            withPermission(PathPerms.PERM_CMD_NG_UNSET_MOD)
        }

        for (modifier in pathfinder.modifierRegistry.types) {
            if (modifier !is ModifierCommandExtension<*>) {
                continue
            }
            var lit: Argument<*> = literalArgument(modifier.subCommandLiteral)
            lit = modifier.registerAddCommand(lit) { mod: Modifier ->
                CommandExecutor { commandSender: CommandSender, args: CommandArguments ->
                    addModifier(commandSender, args.getUnchecked(0)!!, mod)
                }
            }
            set = set.then(lit)
            unset.literalArgument(modifier.subCommandLiteral) {
                executes(CommandExecutor { commandSender: CommandSender, args: CommandArguments ->
                    removeModifier(commandSender, args.getUnchecked(0)!!, modifier.key)
                })
            }
        }

        nodeGroupArgument("group") {
            literalArgument("info") {
                withPermission(PathPerms.PERM_CMD_NG_INFO)
                executes(CommandExecutor { commandSender: CommandSender, args: CommandArguments ->
                    showGroup(commandSender, args.getUnchecked(0))
                })
            }
            then(set)
            then(unset)
        }
    }

    private fun showGroup(sender: CommandSender, group: NodeGroupImpl?) {
        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_NG_INFO.formatted(
                Messages.formatter().namespacedKey("key", group!!.key),
                Messages.formatter().nodeSelection("nodes") { group.resolve().join() },
                Messages.formatter().number("weight", group.weight),
                Messages.formatter().modifiers("modifiers", group.modifiers)
            )
        )
    }

    private fun addModifier(sender: CommandSender, group: NodeGroup, modifier: Modifier) =
        launchIO {
            group.addModifier(modifier)
            pathfinder.storage.saveGroup(group)
            sender.sendMessage(
                Messages.CMD_NG_MODIFY_SET.formatted(
                    Messages.formatter().namespacedKey("group", group.key),
                    Messages.formatter().namespacedKey("type", modifier.key)
                )
            )
        }

    private fun removeModifier(sender: CommandSender, group: NodeGroup, mod: NamespacedKey) =
        launchIO {
            group.removeModifier(mod)
            pathfinder.storage.saveGroup(group)
            sender.sendMessage(
                Messages.CMD_NG_MODIFY_REMOVE.formatted(
                    Messages.formatter().namespacedKey("group", group.key),
                    Messages.formatter().namespacedKey("type", mod)
                )
            )
        }
}
