package de.bossascrew.pathfinder.events.nodegroup;

import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.util.NodeSelection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupRemoveEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final NodeGroup group;
	private final NodeSelection nodes;
	private boolean cancelled;

	public NodeGroupRemoveEvent(NodeGroup group, NodeSelection nodes) {
		this.group = group;
		this.nodes = nodes;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
