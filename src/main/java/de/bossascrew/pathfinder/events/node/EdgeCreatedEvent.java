package de.bossascrew.pathfinder.events.node;

import de.bossascrew.pathfinder.node.Edge;
import de.bossascrew.pathfinder.node.Node;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class EdgeCreatedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Edge edge;

	public EdgeCreatedEvent(Edge edge) {
		this.edge = edge;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
