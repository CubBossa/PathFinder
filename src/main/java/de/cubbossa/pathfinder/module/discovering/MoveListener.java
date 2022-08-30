package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;
import java.util.Date;

public class MoveListener implements Listener {

	// prevents discovering twice
	private Collection<Player> discovering;

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
}
