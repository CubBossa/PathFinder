package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;

import java.time.LocalDateTime;

import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DiscoverListener implements Listener {

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    PathFinderAPI.get().getNodeGroups().thenAccept(nodeGroups -> {
      for (NodeGroup group : nodeGroups) {
        if (!DiscoverHandler.getInstance().fulfillsDiscoveringRequirements(group, event.getPlayer())) {
          continue;
        }
        DiscoverHandler.getInstance().discover(event.getPlayer().getUniqueId(), group, LocalDateTime.now());
      }
    });
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    DiscoverHandler.getInstance().cachePlayer(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    DiscoverHandler.getInstance().invalidatePlayerCache(event.getPlayer().getUniqueId());
  }
}
