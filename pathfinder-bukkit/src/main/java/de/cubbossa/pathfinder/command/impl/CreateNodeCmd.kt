package de.cubbossa.pathfinder.command.impl

import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.command.PathFinderSubCommand
import de.cubbossa.pathfinder.command.locationArgument
import de.cubbossa.pathfinder.command.nodeTypeArgument
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CreateNodeCmd(pathFinder: PathFinder, fallbackWaypointType: () -> NodeType<*>) :
    PathFinderSubCommand(pathFinder, "createnode") {

    init {
        withPermission(PathPerms.PERM_CMD_WP_CREATE)
        executesPlayer(PlayerCommandExecutor { player: Player, _: CommandArguments ->
            createNode(
                player, fallbackWaypointType(),
                BukkitVectorUtils.toInternal(player.location)
            )
        })
        locationArgument("location") {
            displayAsOptional(true)
            executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                createNode(player, fallbackWaypointType(), args.getUnchecked(0)!!)
            })
        }
        nodeTypeArgument("type") {
            executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                createNode(
                    player, args.getUnchecked(0)!!,
                    player.location.toPathFinder()
                )
            })
            locationArgument("location") {
                executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
                    createNode(
                        player, args.getUnchecked(0)!!,
                        args.getUnchecked(1)!!
                    )
                })
            }
        }
    }


    private fun createNode(sender: CommandSender, type: NodeType<*>, location: Location) =
        launchIO {
            pathfinder.storage.createAndLoadNode(type, location).let {
                sender.sendMessage(
                    Messages.CMD_N_CREATE.formatted(Messages.formatter().uuid("id", it.nodeId))
                )
            }
        }
}
