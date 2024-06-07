package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.AbstractPathFinder.Companion.globalGroupKey
import de.cubbossa.pathfinder.AbstractPathFinder.Companion.pathfinder
import de.cubbossa.pathfinder.command.impl.*
import de.cubbossa.pathfinder.discovery.AbstractDiscoveryModule
import de.cubbossa.pathfinder.dump.DumpWriterProvider
import de.cubbossa.pathfinder.group.DiscoverableModifier
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.GraphEditorRegistry
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.util.BukkitUtils
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * The basic command of this plugin, which handles things like reload, export, import, etc.
 */
class PathFinderCommand(private val pathFinder: PathFinder) : CommandTree("pathfinder") {
    /**
     * The basic command of this plugin, which handles things like reload, export, import, etc.
     */
    init {
        withAliases("pf")

        then(NodesCmd(pathFinder))
        val type: NodeType<*>? = pathFinder.nodeTypeRegistry.getType<Node>(pathfinder("waypoint"))
        then(CreateNodeCmd(pathFinder) { type!! })
        then(DeleteNodesCmd(pathFinder))
        then(ListNodesCmd(pathFinder))
        then(NodesCmd(pathFinder))

        then(CreateGroupCmd(pathFinder))
        then(DeleteGroupCmd(pathFinder))
        then(ListGroupsCmd(pathFinder))
        then(GroupCmd(pathFinder))

        then(CreateVisualizerCmd(pathFinder))
        then(DeleteVisualizerCmd(pathFinder))
        then(ImportVisualizerCmd(pathFinder))
        then(ListVisualizersCmd(pathFinder))
        then(VisualizerCmd(pathFinder))

        withRequirement { sender: CommandSender ->
            (sender.hasPermission(PathPerms.PERM_CMD_PF_HELP)
                    || sender.hasPermission(PathPerms.PERM_CMD_PF_INFO)
                    || sender.hasPermission(PathPerms.PERM_CMD_PF_IMPORT_VIS)
                    || sender.hasPermission(PathPerms.PERM_CMD_PF_EXPORT)
                    || sender.hasPermission(PathPerms.PERM_CMD_PF_RELOAD))
        }

        executes(CommandExecutor { sender: CommandSender, args: CommandArguments? ->
            BukkitUtils.wrap(sender).sendMessage(
                Messages.HELP.formatted(
                    Placeholder.parsed("version", PathFinder.get().version)
                )
            )
        })

        literalArgument("createdump") {
            withPermission(PathPerms.PERM_CMD_PF_DUMP)
            executes(CommandExecutor { sender: CommandSender, args: CommandArguments? ->
                try {
                    val dir = PathFinderPlugin.getInstance().dataFolder
                    val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
                    val dump = File(dir, "dump_$date.json")
                    dir.mkdirs()
                    dump.createNewFile()
                    DumpWriterProvider.get().save(dump)

                    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_DUMP_SUCCESS)
                } catch (t: IOException) {
                    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_DUMP_FAIL)
                    pathFinder.logger.log(Level.SEVERE, "Could not create dump file.", t)
                }
            })
        }

        literalArgument("info") {
            withPermission(PathPerms.PERM_CMD_PF_INFO)
            executes(CommandExecutor { commandSender, _ ->
                BukkitUtils.wrap(commandSender).sendMessage(
                    Messages.INFO.formatted(
                        Placeholder.unparsed("version", PathFinder.get().version)
                    )
                )
            })
        }

        literalArgument("modules") {
            withPermission(PathPerms.PERM_CMD_PF_MODULES)
            executes(CommandExecutor { commandSender, _ ->
                val list = PathFinder.get().extensionRegistry.extensions.stream()
                    .map(PathFinderExtension::key)
                    .map { obj: NamespacedKey -> obj.toString() }.toList()
                BukkitUtils.wrap(commandSender).sendMessage(
                    Messages.MODULES.formatted(
                        Messages.formatter().list("modules", list) { content: String? ->
                            Component.text(
                                content!!
                            )
                        }
                    ))
            })
        }

        literalArgument("editmode") {
            executesPlayer(PlayerCommandExecutor { player, args ->
                GraphEditorRegistry.getInstance()
                    .toggleNodeGroupEditor(BukkitUtils.wrap(player), globalGroupKey())
            })
            nodeGroupArgument("group") {
                executesPlayer(PlayerCommandExecutor { player, args ->
                    GraphEditorRegistry.getInstance().toggleNodeGroupEditor(
                        BukkitUtils.wrap(player),
                        (args.getUnchecked(0) as NodeGroup?)?.key
                    )
                }
                )
            }

            literalArgument("help") {
                withPermission(PathPerms.PERM_CMD_PF_HELP)
                executes(CommandExecutor { commandSender, _ ->
                    BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_HELP)
                })
            }

            literalArgument("reload") {
                withPermission(PathPerms.PERM_CMD_PF_RELOAD)

                executes(CommandExecutor { sender, _ ->
                    val now = System.currentTimeMillis()
                    CompletableFuture.runAsync {
                        val pf: AbstractPathFinder = BukkitPathFinder.getInstance()
                        pf.configFileLoader.loadConfig()

                        val translations = pf.translations

                        val fallback = pf.configuration.language.fallbackLanguage
                        translations.clearCache()
                        translations.writeLocale(fallback)
                        translations.loadLocale(fallback)
                    }.whenComplete { _, throwable: Throwable ->
                        if (throwable != null) {
                            BukkitUtils.wrap(sender).sendMessage(
                                Messages.RELOAD_ERROR.formatted(
                                    Placeholder.component(
                                        "error", Component.text(
                                            throwable.message!!.replaceFirst(
                                                "java\\.lang\\.RuntimeException: [^:]*: ".toRegex(),
                                                ""
                                            )
                                        )
                                    )
                                )
                            )
                            PathFinder.get().logger
                                .log(
                                    Level.SEVERE,
                                    "Error occured while reloading files: ",
                                    throwable
                                )
                        } else {
                            BukkitUtils.wrap(sender).sendMessage(
                                Messages.RELOAD_SUCCESS.formatted(
                                    Messages.formatter()
                                        .number("ms", System.currentTimeMillis() - now)
                                )
                            )
                        }
                    }
                })

                literalArgument("language") {
                    executes(CommandExecutor { sender, _ ->
                        val now = System.currentTimeMillis()
                        CompletableFuture.runAsync {
                            val pf: AbstractPathFinder = BukkitPathFinder.getInstance()
                            val translations = pf.translations

                            val fallback = pf.configuration.language.fallbackLanguage
                            translations.clearCache()
                            translations.writeLocale(fallback)
                            translations.loadLocale(fallback)
                        }.whenComplete { unused: Void?, throwable: Throwable? ->
                            if (throwable != null) {
                                BukkitUtils.wrap(sender).sendMessage(
                                    Messages.RELOAD_ERROR.formatted(
                                        Placeholder.component(
                                            "error", Component.text(
                                                throwable.message
                                                    .replaceFirst(
                                                        "java\\.lang\\.RuntimeException: [^:]*: ".toRegex(),
                                                        ""
                                                    )
                                            )
                                        )
                                    )
                                )
                                PathFinder.get().logger
                                    .log(
                                        Level.SEVERE,
                                        "Error occured while reloading files: ",
                                        throwable
                                    )
                            } else {
                                BukkitUtils.wrap(sender).sendMessage(
                                    Messages.RELOAD_SUCCESS_LANG.formatted(
                                        Messages.formatter()
                                            .number("ms", System.currentTimeMillis() - now)
                                    )
                                )
                            }
                        }
                    })
                }

                    .literalArgument("config")
                    .executes { sender, objects ->
                        val now = System.currentTimeMillis()
                        CompletableFuture.runAsync {
                            try {
                                // TODO bah
                                (PathFinder.get() as AbstractPathFinder).configFileLoader.loadConfig()
                            } catch (t: Throwable) {
                                throw RuntimeException(t)
                            }
                        }.whenComplete { unused: Void?, throwable: Throwable? ->
                            if (throwable != null) {
                                BukkitUtils.wrap(sender).sendMessage(
                                    Messages.RELOAD_ERROR.formatted(
                                        TagResolver.builder()
                                            .resolver(
                                                Placeholder.component(
                                                    "error", Component.text(
                                                        throwable.message
                                                            .replaceFirst(
                                                                "java\\.lang\\.RuntimeException: [^:]*: ".toRegex(),
                                                                ""
                                                            )
                                                    )
                                                )
                                            )
                                            .build()
                                    )
                                )
                                PathFinder.get().logger
                                    .log(
                                        Level.SEVERE,
                                        "Error occured while reloading configuration: ",
                                        throwable
                                    )
                            } else {
                                BukkitUtils.wrap(sender).sendMessage(
                                    Messages.RELOAD_SUCCESS_CFG.formatted(
                                        Messages.formatter()
                                            .number("ms", System.currentTimeMillis() - now)
                                    )
                                )
                            }
                        }
                    }
                )
                )

                literalArgument("forcefind")
                    .withGeneratedHelp()
                    .withPermission(PathPerms.PERM_CMD_PF_FORCEFIND)
                    .then(
                        Arguments.pathPlayers("player")
                            .withGeneratedHelp()
                            .then(
                                Arguments.discoverableArgument("discovering")
                                    .executes { commandSender, args ->
                                        for (player in args.< Collection < PathPlayer < Player > > > getUnchecked < kotlin . collections . MutableCollection < PathPlayer < org . bukkit . entity . Player ? > ? > ? > 0) {
                                            onForceFind(commandSender, player, args.getUnchecked(1))
                                        }
                                    })
                    )
                )
                literalArgument("forceforget")
                    .withGeneratedHelp()
                    .withPermission(PathPerms.PERM_CMD_PF_FORCEFORGET)
                    .then(
                        Arguments.pathPlayers("player")
                            .withGeneratedHelp()
                            .then(
                                Arguments.discoverableArgument("discovering")
                                    .executes { commandSender, args ->
                                        for (player in args.< Collection < PathPlayer < Player > > > getUnchecked < kotlin . collections . MutableCollection < PathPlayer < org . bukkit . entity . Player ? > ? > ? > 0) {
                                            onForceForget(
                                                BukkitUtils.wrap(commandSender),
                                                player,
                                                args.getUnchecked(1)
                                            )
                                        }
                                    })
                    )
                )
                literalArgument("worldid")
                    .executesPlayer { sender, args ->
                        BukkitUtils.wrap(sender).sendMessage(
                            Component.text(sender.getWorld().getUID().toString())
                                .clickEvent(
                                    ClickEvent.copyToClipboard(
                                        sender.getWorld().getUID().toString()
                                    )
                                )
                        )
                    })
            }

            private fun onForceFind(
                sender: CommandSender,
                target: PathPlayer<Player>,
                discoverable: NamespacedKey
            ) {
                pathFinder.storage.loadGroup(discoverable)
                    .thenApply(Optional::orElseThrow)
                    .thenAccept { group ->
                        val mod: Optional<DiscoverableModifier> =
                            group.getModifier(DiscoverableModifier.KEY)
                        if (mod.isEmpty) {
                            return@thenAccept
                        }

                        AbstractDiscoveryModule.< Player > getInstance < org . bukkit . entity . Player ? > ().discover(
                            target,
                            group,
                            LocalDateTime.now()
                        )
                        BukkitUtils.wrap(sender).sendMessage(
                            Messages.CMD_FORCE_FIND.formatted(
                                Placeholder.component("name", target.displayName!!),
                                Placeholder.component("discovery", mod.get().displayName)
                            )
                        )
                    }
            }

            private fun onForceForget(
                sender: PathPlayer<CommandSender>,
                target: PathPlayer<Player>,
                discoverable: NamespacedKey
            ) {
                pathFinder.storage.loadGroup(discoverable)
                    .thenApply(Optional::orElseThrow)
                    .thenAccept { group ->
                        val mod: Optional<DiscoverableModifier> =
                            group.getModifier(DiscoverableModifier.KEY)
                        if (mod.isEmpty) {
                            return@thenAccept
                        }

                        AbstractDiscoveryModule.< Player > getInstance < org . bukkit . entity . Player ? > ().forget(
                            target,
                            group
                        )
                        sender.sendMessage(
                            Messages.CMD_FORCE_FORGET.formatted(
                                Placeholder.unparsed("name", target.name!!),
                                Placeholder.component("name", target.displayName!!),
                                Placeholder.component("discovery", mod.get().displayName)
                            )
                        )
                    }
                    .exceptionally { throwable ->
                        throwable.printStackTrace()
                        null
                    }
            }
        }
