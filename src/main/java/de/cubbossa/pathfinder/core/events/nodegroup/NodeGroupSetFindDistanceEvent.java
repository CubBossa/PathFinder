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
public class NodeGroupSetFindDistanceEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final NodeGroup group;
	private float value;
	private boolean cancelled;

	public NodeGroupSetFindDistanceEvent(NodeGroup group, float value) {
		this.group = group;
		this.value = value;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
