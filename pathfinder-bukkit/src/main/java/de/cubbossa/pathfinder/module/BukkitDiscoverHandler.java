package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDateTime;

public class BukkitDiscoverHandler extends AbstractDiscoverHandler<Player> implements Listener {

  private final PathFinder pathFinder;

  public BukkitDiscoverHandler(CommonPathFinder plugin) {
    super(plugin);
    pathFinder = PathFinderProvider.get();
    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    pathFinder.getStorage().loadAllGroups().thenAccept(nodeGroups -> {
      PathPlayer<Player> player = BukkitPathFinder.wrap(event.getPlayer());
      for (NodeGroup group : nodeGroups) {
        if (!super.fulfillsDiscoveringRequirements(group, player)) {
          continue;
        }
        super.discover(player, group, LocalDateTime.now());
      }
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    pathFinder.getStorage().getCache().getDiscoverInfoCache()
        .invalidate(event.getPlayer().getUniqueId());
  }
}
