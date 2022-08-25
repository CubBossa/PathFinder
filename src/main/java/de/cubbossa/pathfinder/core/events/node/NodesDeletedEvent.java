package de.cubbossa.pathfinder.core.events.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.Node;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class NodesDeletedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Collection<Node> nodes;

	public NodesDeletedEvent(Node node) {
		this.nodes = Lists.newArrayList(node);
	}

	public NodesDeletedEvent(Collection<Node> nodes) {
		this.nodes = new ArrayList<>(nodes);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
