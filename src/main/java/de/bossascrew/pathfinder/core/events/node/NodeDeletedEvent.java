package de.bossascrew.pathfinder.core.events.node;

import de.bossascrew.pathfinder.core.node.Node;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeDeletedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Node node;

	public NodeDeletedEvent(Node node) {
		this.node = node;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
