package de.bossascrew.pathfinder.module.discovering;

import de.bossascrew.pathfinder.core.node.NodeGroup;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Date;

public class MoveListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMapsFindable(event.getPlayer().getWorld())) {
			for (NodeGroup group : roadMap.getGroups()) {
				if (!group.isDiscoverable()) {
					continue;
				}
				if (!group.fulfillsDiscoveringRequirements(event.getPlayer())) {
					continue;
				}
				PlayerDiscoverEvent e = new PlayerDiscoverEvent(event.getPlayer(), group, new Date());
				Bukkit.getPluginManager().callEvent(e);
				if(e.isCancelled()) {
					continue;
				}
				DiscoverHandler.getInstance().discover(e.getPlayer().getUniqueId(), group, e.getDate());
			}
		}
	}
}
