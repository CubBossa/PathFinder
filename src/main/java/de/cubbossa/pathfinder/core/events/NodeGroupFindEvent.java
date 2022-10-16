package de.cubbossa.pathfinder.core.events;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

public class NodeGroupFindEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final UUID playerId;
	@Getter
	private final Waypoint triggeringFindable;
	@Getter
	@Setter
	private @Nullable
	NodeGroup group;
	@Getter
	@Setter
	private Date date;
	@Getter
	@Setter
	private boolean cancelled;

	public NodeGroupFindEvent(UUID playerId, NodeGroup group, Waypoint triggeringFindable, Date date) {
		this.playerId = playerId;
		this.group = group;
		this.triggeringFindable = triggeringFindable;
		this.date = date;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
