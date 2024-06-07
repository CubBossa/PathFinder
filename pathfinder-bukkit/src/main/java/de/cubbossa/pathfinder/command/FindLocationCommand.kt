package de.cubbossa.pathfinder.command

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathPerms
import de.cubbossa.pathfinder.asPathPlayer
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.launchIO
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.navigation.NavigationLocation
import de.cubbossa.pathfinder.navigation.NavigationModule.Companion.get
import de.cubbossa.pathfinder.navigation.Route
import de.cubbossa.pathfinder.node.implementation.PlayerNode
import de.cubbossa.pathfinder.node.implementation.Waypoint
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.kotlindsl.locationArgument
import org.bukkit.entity.Player
import java.util.*
import java.util.logging.Level

class FindLocationCommand : CommandTree("findlocation") {
    init {
        withAliases("gpslocation", "navigatelocation")
        withPermission(PathPerms.PERM_CMD_FIND_LOCATION)

        locationArgument("location") {
            executesPlayer(
                PlayerCommandExecutor { player, args ->
                    val target: Location = args.getUnchecked(0)!!
                    val waypoint = Waypoint(UUID.randomUUID(), target)

                    val p = player.asPathPlayer()
                    launchIO {
                        val navigationModule = get<Player>()
                        try {
                            val path = navigationModule.navigate(
                                p, Route
                                    .from(NavigationLocation.movingExternalNode(PlayerNode(p)))
                                    .to(NavigationLocation.movingExternalNode(waypoint))
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
                }
            )
        }
    }
}
