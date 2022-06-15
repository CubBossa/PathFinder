package de.bossascrew.pathfinder.events;

import de.bossascrew.pathfinder.node.Waypoint;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Date;
import java.util.UUID;

public class NodeFindEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final UUID playerId;
	@Getter @Setter
	private Waypoint findable;
	@Getter @Setter
	private Date date;
	@Getter @Setter
	private boolean cancelled;

	public NodeFindEvent(UUID playerId, Waypoint findable, Date date) {
		this.playerId = playerId;
		this.findable = findable;
		this.date = date;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
