package de.bossascrew.pathfinder.events.nodegroup;

import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeGroupDeletedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final NodeGroup group;

	public NodeGroupDeletedEvent(NodeGroup group) {
		this.group = group;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
