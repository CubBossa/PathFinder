package de.cubbossa.pathfinder.listener

import de.cubbossa.pathfinder.asPathPlayer
import de.cubbossa.pathfinder.node.GraphEditorRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player.asPathPlayer()

        val currentlyEdited = GraphEditorRegistry.getInstance().getEdited(player)
        currentlyEdited?.let {
            GraphEditorRegistry.getInstance()
                .getNodeGroupEditor<Player>(currentlyEdited)
                .thenAccept { it.setEditMode(player, false) }
        }
    }
}
