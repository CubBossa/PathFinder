package de.bossascrew.pathfinder.core.events.node;

import de.bossascrew.pathfinder.core.node.Edge;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Getter
public class EdgesCreatedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Collection<Edge> edges;

	public EdgesCreatedEvent(Edge... edges) {
		this.edges = new HashSet<>();
		this.edges.addAll(List.of(edges));
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
