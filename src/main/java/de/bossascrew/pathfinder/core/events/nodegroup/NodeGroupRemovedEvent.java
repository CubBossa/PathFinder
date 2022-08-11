package de.bossascrew.pathfinder.core.events.nodegroup;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.core.node.Groupable;
import de.bossascrew.pathfinder.core.node.NodeGroup;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class NodeGroupRemovedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Collection<Groupable> groupables;
	private final Collection<NodeGroup> groups;

	public NodeGroupRemovedEvent(Groupable groupables, NodeGroup groups) {
		this(Lists.newArrayList(groupables), List.of(groups));
	}

	public NodeGroupRemovedEvent(Collection<Groupable> groupables, Collection<NodeGroup> groups) {
		this.groupables = Collections.unmodifiableCollection(groupables);
		this.groups = Collections.unmodifiableCollection(groups);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public String toString() {
		return "NodeGroupRemovedEvent{" +
				"groupables=" + groupables +
				", groups=" + groups +
				'}';
	}
}
