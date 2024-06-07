package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.command.*
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.misc.Pagination
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeSelection
import de.cubbossa.pathfinder.storage.getGroups
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import de.cubbossa.pathfinder.util.NodeUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class NodesCmd(pathFinder: PathFinder?) : PathFinderSubCommand(pathFinder!!, "nodes") {

    companion object {
        val PERMS = listOf(
            PathPerms.PERM_CMD_WP_INFO,
            PathPerms.PERM_CMD_WP_LIST,
            PathPerms.PERM_CMD_WP_CREATE,
            PathPerms.PERM_CMD_WP_DELETE,
            PathPerms.PERM_CMD_WP_TPHERE,
            PathPerms.PERM_CMD_WP_TP,
            PathPerms.PERM_CMD_WP_CONNECT,
            PathPerms.PERM_CMD_WP_DISCONNECT,
            PathPerms.PERM_CMD_WP_SET_CURVE,
            PathPerms.PERM_CMD_WP_ADD_GROUP,
            PathPerms.PERM_CMD_WP_REMOVE_GROUP,
            PathPerms.PERM_CMD_WP_CLEAR_GROUPS,
        )
    }


    init {
        withGeneratedHelp()

        withRequirement { sender: CommandSender ->
            PERMS.stream().anyMatch { sender.hasPermission(it) }
        }

        nodeSelectionArgument("nodes") {
            literalArgument("info") {
                withPermission(PathPerms.PERM_CMD_WP_INFO)
                executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                    onInfo(player, args.getUnchecked(0)!!)
                })
            }
            literalArgument("tphere") {
                withPermission(PathPerms.PERM_CMD_WP_TPHERE)
                executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                    teleportNodes(
                        player,
                        args.getUnchecked(0)!!,
                        BukkitVectorUtils.toInternal(player.location)
                    )
                })
            }
            literalArgument("tp") {
                withPermission(PathPerms.PERM_CMD_WP_TP)
                locationArgument("location") {
                    executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                        teleportNodes(
                            player,
                            args.getUnchecked(0)!!,
                            args.getUnchecked(1)!!
                        )
                    })
                }
            }
            literalArgument("connect") {
                withPermission(PathPerms.PERM_CMD_WP_CONNECT)
                nodeSelectionArgument("end") {
                    executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                        connectNodes(player, args.getUnchecked(0)!!, args.getUnchecked(1)!!)
                    })
                }
            }
            literalArgument("disconnect") {
                withPermission(PathPerms.PERM_CMD_WP_DISCONNECT)
                nodeSelectionArgument("end") {
                    executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                        disconnectNodes(
                            player,
                            args.getUnchecked(0)!!,
                            args.getUnchecked(1)!!
                        )
                    })
                }
            }
            literalArgument("groups") {
                literalArgument("add") {
                    withPermission(PathPerms.PERM_CMD_WP_ADD_GROUP)
                    nodeGroupArgument("group") {
                        executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                            addGroup(
                                player,
                                args.getUnchecked(0)!!,
                                args.getUnchecked(1)!!
                            )
                        })
                    }
                }
                literalArgument("remove") {
                    withPermission(PathPerms.PERM_CMD_WP_REMOVE_GROUP)
                    nodeGroupArgument("group") {
                        executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                            removeGroup(
                                player,
                                args.getUnchecked(0)!!,
                                args.getUnchecked(1)!!
                            )
                        })
                    }
                }
                literalArgument("clear") {
                    withPermission(PathPerms.PERM_CMD_WP_CLEAR_GROUPS)
                    executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                        clearGroups(player, args.getUnchecked(0)!!)
                    })
                }
            }
        }
    }

    private fun addGroup(sender: CommandSender, nodes: NodeSelection, group: NodeGroup) = launchIO {
        group.addAll(nodes.ids)
        pathfinder.storage.saveGroup(group)

        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_ADD_GROUP.formatted(
                Messages.formatter().nodeSelection("nodes") { nodes },
                Messages.formatter().namespacedKey("group", group.key)
            )
        )
    }

    private fun removeGroup(
        sender: CommandSender,
        nodes: NodeSelection,
        group: NodeGroup
    ) = launchIO {
        group.removeAll(nodes.ids.toSet())
        pathfinder.storage.saveGroup(group)

        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_REMOVE_GROUP.formatted(
                Messages.formatter().nodeSelection("nodes") { nodes }
            ))
    }

    public inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> =
        transform { value ->
            return@transform emit(transform(value))
        }

    private fun clearGroups(sender: CommandSender, nodes: NodeSelection) = launchIO {
        val storage = pathfinder.storage
        nodes.asFlow()
            .map { storage.getGroups(it) }
            .flatMapMerge { it.asFlow() }
            .onEach { g -> nodes.ids.forEach { g.remove(it) } }
            .onEach { storage.saveGroup(it) }
            .collect()
        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_CLEAR_GROUPS.formatted(
                Messages.formatter().nodeSelection("nodes") { nodes }
            )
        )
    }

    private fun disconnectNodes(
        sender: CommandSender,
        start: NodeSelection,
        end: NodeSelection
    ) = launchIO {
        for (s in start) {
            for (e in end) {
                s.disconnect(e)
                pathfinder.storage.saveNode(s)
            }
        }
        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_DISCONNECT.formatted(
                Messages.formatter().nodeSelection("start") { start },
                Messages.formatter().nodeSelection("end") { end }
            ))
    }

    private fun connectNodes(
        sender: CommandSender,
        start: NodeSelection,
        end: NodeSelection
    ) = launchIO {
        for (s in start) {
            for (e in end) {
                if (s == e) {
                    continue
                }
                if (s.hasConnection(e)) {
                    continue
                }
                s.connect(e)
                pathfinder.storage.saveNode(s)
            }
        }
        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_CONNECT.formatted(
                Messages.formatter().nodeSelection("start") { start },
                Messages.formatter().nodeSelection("end") { end }
            ))
    }

    private fun teleportNodes(
        sender: CommandSender,
        nodes: NodeSelection,
        location: Location
    ) = launchIO {
        val jobs = ArrayList<Job>()
        for (node in nodes) {
            node.location = location
            jobs.add(async {
                pathfinder.storage.saveNode(node)
            })
        }
        jobs.joinAll()
        BukkitUtils.wrap(sender).sendMessage(
            Messages.CMD_N_UPDATED.formatted(
                Messages.formatter().nodeSelection("selection") { nodes }
            )
        )
    }

    private fun onInfo(player: Player, selection: NodeSelection) = launchIO {
        if (selection.size == 0) {
            BukkitUtils.wrap(player).sendMessage(Messages.CMD_N_INFO_NO_SEL)
            return@launchIO
        }
        if (selection.size > 1) {
            NodeUtils.onList(player, selection, Pagination.page(0, 10))
            return@launchIO
        }
        val node = selection[0]

        val neighbours: Collection<UUID> = node.edges.stream().map(Edge::end).toList()
        val resolvedNeighbours: Collection<Node> =
            pathfinder.storage.loadNodes(neighbours)

        val message = Messages.CMD_N_INFO.formatted(
            Messages.formatter().uuid("id", node.nodeId),
            Messages.formatter().vector("position", node.location),
            Placeholder.unparsed("world", node.location.world.name),
            Messages.formatter().nodeSelection("edges") { resolvedNeighbours }
        )
        BukkitUtils.wrap(player).sendMessage(message)
    }
}
