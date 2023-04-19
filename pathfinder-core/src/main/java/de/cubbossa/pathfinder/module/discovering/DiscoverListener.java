package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.storage.Storage;
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
        if (!DiscoverHandler.getInstance().fulfillsDiscoveringRequirements(group, PathPlugin.wrap(event.getPlayer()))) {
          continue;
        }
        DiscoverHandler.getInstance().discover(event.getPlayer().getUniqueId(), group, LocalDateTime.now());
      }
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    //TODO cache in interface
    ((Storage) pathFinder.getStorage()).getDiscoverInfoCache().invalidate(event.getPlayer().getUniqueId());
  }
}
