package de.bossascrew.pathfinder.module.visualizing.events;

import de.bossascrew.pathfinder.module.visualizing.ParticlePath;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
public class PathTargetFoundEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final UUID playerId;
	private final ParticlePath path;

	public PathTargetFoundEvent(UUID playerId, ParticlePath path) {
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
