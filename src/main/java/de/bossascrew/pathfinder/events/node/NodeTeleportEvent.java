package de.bossascrew.pathfinder.events.node;

import de.bossascrew.pathfinder.node.Node;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

@Getter
@Setter
public class NodeTeleportEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Node node;
	private final Vector newPosition;
	private Vector newPositionModified;
	private boolean cancelled = false;

	public NodeTeleportEvent(Node node, Vector newPosition) {
		this.node = node;
		this.newPosition = newPosition;
		this.newPositionModified = newPosition;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
