package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.DiscoverHandler;
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
        if (!DiscoverHandler.getInstance()
            .fulfillsDiscoveringRequirements(group, PathPlugin.wrap(event.getPlayer()))) {
          continue;
        }
        DiscoverHandler.getInstance()
            .discover(event.getPlayer().getUniqueId(), group, LocalDateTime.now());
      }
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    pathFinder.getStorage().getCache().getDiscoverInfoCache().invalidate(event.getPlayer().getUniqueId());
  }
}
