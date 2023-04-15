package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.util.VectorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

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
