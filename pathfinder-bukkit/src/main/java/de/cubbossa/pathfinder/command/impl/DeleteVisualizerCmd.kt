package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.pathVisualizerArgument
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.logging.Level

class DeleteVisualizerCmd(pathFinder: PathFinder) :
    PathFinderSubCommand(pathFinder, "deletevisualizer") {
    init {
        withGeneratedHelp()
        withPermission(PathPerms.PERM_CMD_PV_DELETE)
        pathVisualizerArgument("visualizer") {
            executes(CommandExecutor { commandSender, objects ->
                onDelete(commandSender, objects.getUnchecked(0)!!)
            })
        }
    }

    private fun onDelete(sender: CommandSender, visualizer: PathVisualizer<*, *>) =
        launchIO {
            try {
                pathfinder.storage.deleteVisualizer(visualizer)
                BukkitUtils.wrap(sender).sendMessage(
                    Messages.CMD_VIS_DELETE_SUCCESS.formatted(
                        Messages.formatter().namespacedKey("key", visualizer.key)
                    )
                )
            } catch (throwable: Throwable) {
                BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_DELETE_ERROR)
                pathfinder.logger.log(Level.WARNING, "Could not delete visualizer", throwable)
            }
        }
}
