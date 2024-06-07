package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.AbstractPathFinder.Companion.pathfinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.visualizerTypeArgument
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

class CreateVisualizerCmd(pathFinder: PathFinder) :
    PathFinderSubCommand(pathFinder, "createvisualizer") {
    init {
        withGeneratedHelp()
        withPermission(PathPerms.PERM_CMD_PV_CREATE)

        visualizerTypeArgument("type") {
            stringArgument("key") {
                executes(CommandExecutor { commandSender, objects ->
                    onCreate(
                        commandSender,
                        objects.getUnchecked(0)!!,
                        pathfinder(objects.getUnchecked(1))
                    )
                })
            }
        }
    }

    private fun onCreate(
        sender: CommandSender,
        type: VisualizerType<out PathVisualizer<*, *>>,
        key: NamespacedKey
    ) {
        launchIO {
            val vis = pathfinder.storage.loadVisualizer<PathVisualizer<*, *>>(key)
            if (vis != null) {
                BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_NAME_EXISTS)
                return@launchIO
            }
            val visualizer = pathfinder.storage.createAndLoadVisualizer(type, key)
            BukkitUtils.wrap(sender).sendMessage(
                Messages.CMD_VIS_CREATE_SUCCESS.formatted(
                    Messages.formatter().namespacedKey("key", visualizer.key),
                    Placeholder.component("type", Component.text(type.commandName))
                )
            )
        }
    }
}
