package de.bossascrew.pathfinder.events;

import de.bossascrew.pathfinder.data.findable.Findable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

public class NodeFindEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final UUID playerId;
	@Getter @Setter
	private Findable findable;
	@Getter @Setter
	private Date date;
	@Getter @Setter
	private boolean cancelled;

	public NodeFindEvent(UUID playerId, Findable findable, Date date) {
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
