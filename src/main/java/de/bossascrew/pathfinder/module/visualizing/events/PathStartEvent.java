package de.bossascrew.pathfinder.module.visualizing.events;

import de.bossascrew.pathfinder.module.visualizing.ParticlePath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
public class PathStartEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final UUID playerId;
	private final ParticlePath path;
	private Location target;
	private float distance;
	private boolean cancelled = false;

	public PathStartEvent(UUID playerId, ParticlePath path, Location target, float distance) {
		this.playerId = playerId;
		this.path = path;
		this.target = target;
		this.distance = distance;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
