package pathfinder.old.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import pathfinder.old.RoadMap;

public class PlayerMoveListener implements Listener {
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		for(RoadMap rm : RoadMap.getRoadMaps()) {
			if(!e.getPlayer().getWorld().equals(rm.getWorld())) return;
			if(rm.getPathFinder().checkPlayer(e.getPlayer(), rm.getFile().getRadian())) {
				rm.getPathFinder().stopPath(e.getPlayer().getUniqueId());
				e.getPlayer().sendMessage("�aZiel erreicht!");
			}
		}

	}
}
