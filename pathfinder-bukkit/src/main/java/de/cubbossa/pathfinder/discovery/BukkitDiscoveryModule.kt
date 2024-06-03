package de.cubbossa.pathfinder.discovery

import de.cubbossa.pathfinder.BukkitPathFinder
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderExtension
import de.cubbossa.pathfinder.PathFinderPlugin
import de.cubbossa.pathfinder.command.DiscoveriesCommand
import de.cubbossa.pathfinder.misc.PathPlayer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.pf4j.Extension
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Extension(points = [PathFinderExtension::class])
class BukkitDiscoveryModule : AbstractDiscoveryModule<Player>(), Listener {
    private val playerLock: MutableCollection<UUID> = ConcurrentHashMap.newKeySet()
    private val discoveriesCommand = DiscoveriesCommand()

    override fun onEnable(pathPlugin: PathFinder) {
        super.onEnable(pathPlugin)
        Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance())
        BukkitPathFinder.getInstance().commandRegistry.registerCommand(discoveriesCommand)
    }

    override fun dispose() {
        BukkitPathFinder.getInstance().commandRegistry.unregisterCommand(discoveriesCommand)
        PlayerMoveEvent.getHandlerList().unregister(this)
        super.dispose()
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) = runBlocking {
        val uuid = event.player.uniqueId
        if (playerLock.contains(uuid)) {
            return@runBlocking
        }
        playerLock.add(uuid)

        val player = PathPlayer.wrap(event.player)

        launch {
            val groups = super.getFulfillingGroups(player)
            val jobs: MutableSet<Deferred<*>> = HashSet()
            jobs.add(async {
                groups.forEach { group ->
                    super.discover(player, group, LocalDateTime.now())
                }
            })
            for (job in jobs) {
                job.join()
            }
            playerLock.remove(uuid)
        }
    }
}
