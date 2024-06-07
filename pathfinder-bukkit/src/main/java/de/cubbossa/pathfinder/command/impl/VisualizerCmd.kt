package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.command.*
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class VisualizerCmd(pathFinder: PathFinder) : PathFinderSubCommand(pathFinder, "visualizer") {
    init {
        withGeneratedHelp()


        for (type: VisualizerType<out PathVisualizer<*, *>> in pathFinder.visualizerTypeRegistry.types) {
            if (type !is VisualizerTypeCommandExtension) {
                continue
            }

            val set = LiteralArgument("set")
                .withPermission(PathPerms.PERM_CMD_PV_MODIFY) as LiteralArgument

            set.literalArgument("permission") {
                greedyStringArgument("permission") {
                    executes(CommandExecutor { commandSender: CommandSender, args: CommandArguments ->
                        if (args[0] is PathVisualizer<*, *>) {
                            // just what we do with internal visualizers, but we cannot use property objects here because
                            // we want this code to be abstract and to work with all kind of visualizers.
                            val visualizer = args[0] as PathVisualizer<*, *>

                            val old: String? = visualizer.permission
                            val perm = args.getUnchecked<String>(1)
                            visualizer.permission = perm

                            launchIO {
                                pathFinder.storage.saveVisualizer(visualizer)
                                commandSender.sendMessage(
                                    Messages.CMD_VIS_SET_PROP.formatted(
                                        Messages.formatter()
                                            .namespacedKey("key", visualizer.key),
                                        Messages.formatter().namespacedKey("type", type.key),
                                        Placeholder.parsed("property", "permission"),
                                        Messages.formatter().permission("old-value", old),
                                        Messages.formatter().permission("value", perm)
                                    )
                                )
                            }
                        }
                    })
                }
            }
            type.appendEditCommand(set, 0, 1)
            literalArgument(type.commandName) {
                pathVisualizerArgument("visualizer", type) {
                    then(set)
                    literalArgument("info") {
                        withPermission(PathPerms.PERM_CMD_PV_INFO)
                        executes(CommandExecutor { commandSender: CommandSender?, objects: CommandArguments ->
                            onInfo(
                                commandSender!!,
                                objects.getUnchecked(0)!!
                            )
                        })
                    }
                }
            }
        }
    }

    private fun onInfo(sender: CommandSender, visualizer: PathVisualizer<*, *>) =
        launchIO thenAccept@{
            val p = sender.asPathPlayer()
            val type = pathfinder.storage.loadVisualizerType<PathVisualizer<*, *>>(visualizer.key)
            if (type == null) {
                p.sendMessage(Messages.CMD_VIS_NO_TYPE_FOUND)
                return@thenAccept
            }
            if (type !is VisualizerTypeMessageExtension<*>) {
                p.sendMessage(Messages.CMD_VIS_NO_INFO)
                return@thenAccept
            }

            type.getInfoMessage(visualizer)?.formatted(
                TagResolver.builder()
                    .resolver(Messages.formatter().namespacedKey("key", visualizer.key))
                    .resolver(Messages.formatter().namespacedKey("type", type.key))
                    .resolver(
                        Messages.formatter().permission("permission", visualizer.permission)
                    )
                    .build()
            )?.let { p.sendMessage(it) }
        }
}
