package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.VisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
public class PathCancelEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final UUID playerId;
	private final VisualizerPath path;
	private boolean cancelled = false;

	public PathCancelEvent(UUID playerId, VisualizerPath path) {
		this.playerId = playerId;
		this.path = path;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
