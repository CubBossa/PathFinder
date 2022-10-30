package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class DiscoverListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		for (NodeGroup group : NodeGroupHandler.getInstance().getNodeGroups()) {
			if (!group.isDiscoverable()) {
				continue;
			}
			if (!group.fulfillsDiscoveringRequirements(event.getPlayer())) {
				continue;
			}
			DiscoverHandler.getInstance().discover(event.getPlayer().getUniqueId(), group, new Date());
		}
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
