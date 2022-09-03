package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.VisualizerPath;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
public class PathTargetFoundEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final UUID playerId;
	private final VisualizerPath path;

	public PathTargetFoundEvent(UUID playerId, VisualizerPath path) {
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
