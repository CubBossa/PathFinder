package de.bossascrew.pathfinder.module.visualizing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class MoveListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		new ArrayList<>(FindModule.getInstance().getActivePaths(event.getPlayer()).values()).stream()
				.filter(info -> p.getLocation().distance(info.target()) < info.distance())
				.forEach(info -> FindModule.getInstance().reachTarget(info));
	}
}
