package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
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
