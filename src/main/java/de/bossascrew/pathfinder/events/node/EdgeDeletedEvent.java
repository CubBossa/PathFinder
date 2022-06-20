package de.bossascrew.pathfinder.events.node;

import de.bossascrew.pathfinder.node.Edge;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class EdgeDeletedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Edge edge;

	public EdgeDeletedEvent(Edge edge) {
		this.edge = edge;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
