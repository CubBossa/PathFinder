package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Date;

public class MoveListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		for (NodeGroup group : NodeGroupHandler.getInstance().getNodeGroups()) {
			if (!group.isDiscoverable()) {
				continue;
			}
			if (!group.fulfillsDiscoveringRequirements(event.getPlayer())) {
				continue;
			}
			PlayerDiscoverEvent e = new PlayerDiscoverEvent(event.getPlayer(), group, new Date());
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				continue;
			}
			DiscoverHandler.getInstance().discover(e.getPlayer().getUniqueId(), group, e.getDate());
		}
	}
}