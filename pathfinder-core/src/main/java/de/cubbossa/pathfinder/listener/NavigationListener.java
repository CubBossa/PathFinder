package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.module.FindModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class NavigationListener implements Listener {

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    PathPlayer<Player> pathPlayer = PathPlugin.wrap(p);

    FindModule.SearchInfo info = FindModule.getInstance().getActivePath(pathPlayer);
    if (info != null && pathPlayer.getLocation().distance(info.target()) < info.distance()) {
      FindModule.getInstance().reachTarget(info);
    }
  }
}
