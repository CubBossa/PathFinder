package de.cubbossa.pathfinder.navigation

import de.cubbossa.pathfinder.BukkitPathFinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderExtension
import de.cubbossa.pathfinder.PathFinderPlugin
import de.cubbossa.pathfinder.command.CancelPathCommand
import de.cubbossa.pathfinder.command.FindCommand
import de.cubbossa.pathfinder.command.FindLocationCommand
import de.cubbossa.pathfinder.event.PathStartEvent
import de.cubbossa.pathfinder.event.PathStoppedEvent
import de.cubbossa.pathfinder.misc.PathPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.pf4j.Extension
import kotlin.math.pow

@Extension(points = [PathFinderExtension::class])
class BukkitNavigationModule : AbstractNavigationModule<Player>(), Listener {
    private var findCommand: FindCommand? = null
    private var findLocationCommand: FindLocationCommand? = null
    private var cancelPathCommand: CancelPathCommand? = null

    override fun onLoad(pathPlugin: PathFinder) {
        super.onLoad(pathPlugin)

        findCommand = FindCommand()
        findLocationCommand = FindLocationCommand()
        cancelPathCommand = CancelPathCommand()

        if (pathPlugin is BukkitPathFinder) {
            // TODO
            pathPlugin.commandRegistry.registerCommand(findCommand)
            pathPlugin.commandRegistry.registerCommand(findLocationCommand)
            pathPlugin.commandRegistry.registerCommand(cancelPathCommand)
        }
    }

    override fun onEnable(pathPlugin: PathFinder) {
        super.onEnable(pathPlugin)

        Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance())
        eventDispatcher!!.listen(PathStartEvent::class.java) { e: PathStartEvent<*> ->
            cancelPathCommand!!.refresh(e.player)
        }
        eventDispatcher!!.listen(PathStoppedEvent::class.java) { e: PathStoppedEvent<*> ->
            cancelPathCommand!!.refresh(e.player)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val p = event.player
        val pathPlayer = PathPlayer.wrap(p)

        val info = activePaths[pathPlayer.uniqueId]
        if (info != null && pathPlayer.location.distanceSquared(info.target().location) < info.dist()
                .pow(info.dist())
        ) {
            reach(info.playerId())
        }
    }
}
