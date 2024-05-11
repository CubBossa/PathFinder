package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.GraphEditorRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    PathPlayer<Player> player = PathPlayer.wrap(event.getPlayer());

    NamespacedKey currentlyEdited = GraphEditorRegistry.getInstance().getEdited(player);
    if (currentlyEdited != null) {
      GraphEditorRegistry.getInstance().<Player>getNodeGroupEditor(currentlyEdited)
          .thenAccept(e -> e.setEditMode(player, false));
    }
  }
}
