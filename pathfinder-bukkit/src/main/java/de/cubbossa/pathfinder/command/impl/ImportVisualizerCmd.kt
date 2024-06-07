package de.cubbossa.pathfinder.command.impl

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.examples.ExamplesFileReader.ExampleFile
import de.cubbossa.pathfinder.examples.ExamplesLoader
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import org.bukkit.command.CommandSender
import java.util.*
import java.util.function.Consumer

class ImportVisualizerCmd(pathFinder: PathFinder) :
    PathFinderSubCommand(pathFinder, "importvisualizer") {
    private val loader: ExamplesLoader

    init {
        withPermission(PathPerms.PERM_CMD_PF_IMPORT_VIS)
        withGeneratedHelp()

        loader = ExamplesLoader(pathFinder.visualizerTypeRegistry)

        greedyStringArgument("name") {
            replaceSuggestions { _, suggestionsBuilder: SuggestionsBuilder ->
                loader.exampleFiles.thenApply { files: Collection<ExampleFile> ->
                    files.stream()
                        .map { it.name }
                        .forEach { text: String? -> suggestionsBuilder.suggest(text) }
                    suggestionsBuilder.suggest("*")
                    suggestionsBuilder.build()
                }
            }
            executes(CommandExecutor { commandSender: CommandSender, objects: CommandArguments ->
                if (objects.getUnchecked<String>(0) == "*") {
                    loader.exampleFiles.thenAccept { files: Collection<ExampleFile> ->
                        files
                            .forEach(Consumer { exampleFile: ExampleFile ->
                                importVisualizer(
                                    commandSender,
                                    exampleFile
                                )
                            })
                    }
                    return@CommandExecutor
                }
                loader.exampleFiles
                    .thenApply { files: Collection<ExampleFile?> ->
                        files.stream().filter { f: ExampleFile? ->
                            f!!.name.equals(
                                objects.getUnchecked(0),
                                ignoreCase = true
                            )
                        }
                            .findFirst()
                    }
                    .thenAccept { exampleFile: ExampleFile? ->
                        importVisualizer(
                            commandSender,
                            exampleFile
                        )
                    }
                    .exceptionally { throwable: Throwable? ->
                        BukkitUtils.wrap(commandSender).sendMessage(Messages.throwable(throwable))
                        null
                    }
            })
        }
    }

    private fun importVisualizer(
        commandSender: CommandSender,
        exampleFile: ExampleFile?
    ) = launchIO {
        if (exampleFile == null) {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_NOT_EXISTS)
            return@launchIO
        }
        val key = fromString(exampleFile.name.replace(".yml", "").replace("$", ":"))
        val pathVisualizer = pathfinder.storage.loadVisualizer<PathVisualizer<*, *>>(key)
        if (pathVisualizer != null) {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_EXISTS)
            return@launchIO
        }
        loader
            .loadVisualizer<PathVisualizer<*, *>>(exampleFile)
            .thenCompose { v: Map.Entry<PathVisualizer<*, *>, VisualizerType<PathVisualizer<*, *>>> ->
                save(
                    v.value,
                    v.key
                )
            }
            .thenAccept {
                BukkitUtils.wrap(commandSender).sendMessage(
                    Messages.CMD_VIS_IMPORT_SUCCESS.formatted(
                        Messages.formatter().namespacedKey("key", key)
                    )
                )
            }
            .exceptionally { throwable: Throwable? ->
                BukkitUtils.wrap(commandSender).sendMessage(
                    Messages.GEN_ERROR.formatted(
                        Messages.formatter().throwable(throwable)
                    )
                )
                null
            }
    }

    private fun <V : PathVisualizer<*, *>> save(
        type: VisualizerType<V>,
        vis: V
    ) = launchIO {
        pathFinder.storage.createAndLoadVisualizer(type, vis.key)
        pathFinder.storage.saveVisualizer(vis)
    }
}
