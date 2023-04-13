package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class DiscoverListener implements Listener {

  private final PathFinder pathFinder;

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    pathFinder.getStorage().loadAllGroups().thenAccept(nodeGroups -> {
      for (NodeGroup group : nodeGroups) {
        if (!DiscoverHandler.getInstance().fulfillsDiscoveringRequirements(group, event.getPlayer())) {
          continue;
        }
        DiscoverHandler.getInstance().discover(event.getPlayer().getUniqueId(), group, LocalDateTime.now());
      }
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    pathFinder.getStorage().getDiscoverInfoCache().invalidate(event.getPlayer().getUniqueId());
  }
}
