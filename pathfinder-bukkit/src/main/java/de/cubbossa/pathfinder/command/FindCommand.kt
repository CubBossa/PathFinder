package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.asPathPlayer
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.navigation.NavigationLocation
import de.cubbossa.pathfinder.navigation.NavigationModule.Companion.get
import de.cubbossa.pathfinder.navigation.Route
import de.cubbossa.pathfinder.node.NodeSelectionImpl
import de.cubbossa.pathfinder.node.implementation.PlayerNode
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player
import java.util.logging.Level

class FindCommand : CommandTree("find") {
    init {
        withAliases("gps", "navigate")
        withPermission(PathPerms.PERM_CMD_FIND)

        navigateSelectionArgument("selection") {
            executesPlayer(PlayerCommandExecutor { player, args ->
                val targets: NodeSelectionImpl =
                    args.getUnchecked(0) ?: return@PlayerCommandExecutor
                val p: PathPlayer<Player> = player.asPathPlayer()
                if (targets.isEmpty()) {
                    p.sendMessage(Messages.CMD_FIND_EMPTY)
                    return@PlayerCommandExecutor
                }
                launchIO {
                    val navigationModule = get<Player>()
                    try {
                        val path = navigationModule.navigate(
                            p, Route
                                .from(NavigationLocation.movingExternalNode(PlayerNode(p)))
                                .toAny(targets)
                        )
                        navigationModule.cancelPathWhenTargetReached(path)
                    } catch (t: NoPathFoundException) {
                        p.sendMessage(Messages.CMD_FIND_BLOCKED)
                    } catch (t: GraphEntryNotEstablishedException) {
                        p.sendMessage(Messages.CMD_FIND_TOO_FAR)
                    } catch (t: Throwable) {
                        p.sendMessage(Messages.CMD_FIND_UNKNOWN)
                        PathFinder.get().logger.log(
                            Level.SEVERE,
                            "Unknown error while finding path.",
                            t
                        )
                    }
                }
            })
        }
    }
}
