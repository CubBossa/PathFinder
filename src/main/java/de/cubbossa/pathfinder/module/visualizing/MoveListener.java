package de.cubbossa.pathfinder.module.visualizing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    var info = FindModule.getInstance().getActivePath(p);
    if (info != null && p.getLocation().distance(info.target()) < info.distance()) {
      FindModule.getInstance().reachTarget(info);
    }
  }
}
