package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@AllArgsConstructor
public class NodeGroupSetDiscoverableEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final NodeGroup group;
	private final boolean newValue;
	private boolean cancelled;

	public NodeGroupSetDiscoverableEvent(NodeGroup group, boolean value) {
		this.group = group;
		this.newValue = value;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
