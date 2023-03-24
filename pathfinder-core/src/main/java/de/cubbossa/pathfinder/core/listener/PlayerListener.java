package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.core.node.NodeHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    NamespacedKey currentlyEdited = NodeHandler.getInstance().getEdited(event.getPlayer());
    if (currentlyEdited != null) {
      NodeHandler.getInstance().getNodeGroupEditor(currentlyEdited)
          .thenAccept(e -> e.setEditMode(event.getPlayer().getUniqueId(), false));
    }
  }
}
