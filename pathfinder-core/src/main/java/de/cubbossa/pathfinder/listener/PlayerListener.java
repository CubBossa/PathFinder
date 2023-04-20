package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    PathPlayer<Player> player = PathPlugin.wrap(event.getPlayer());

    NamespacedKey currentlyEdited = NodeHandler.getInstance().getEdited(player);
    if (currentlyEdited != null) {
      NodeHandler.getInstance().getNodeGroupEditor(currentlyEdited)
          .thenAccept(e -> e.setEditMode(player, false));
    }
  }
}
