package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    NamespacedKey currentlyEdited =
        RoadMapHandler.getInstance().getRoadMapEditedBy(event.getPlayer());
    if (currentlyEdited != null) {
      RoadMapHandler.getInstance().getRoadMapEditor(currentlyEdited)
          .setEditMode(event.getPlayer().getUniqueId(), false);
    }
  }
}
