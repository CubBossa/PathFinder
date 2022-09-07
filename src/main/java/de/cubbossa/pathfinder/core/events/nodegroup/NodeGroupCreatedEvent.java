package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeGroupCreatedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final NodeGroup group;

	public NodeGroupCreatedEvent(NodeGroup group) {
		this.group = group;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
