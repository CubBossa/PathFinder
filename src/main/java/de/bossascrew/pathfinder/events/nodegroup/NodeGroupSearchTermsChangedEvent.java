package de.bossascrew.pathfinder.events.nodegroup;

import de.bossascrew.pathfinder.node.NodeGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collection;

@Getter

public class NodeGroupSearchTermsChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public enum Action {
		ADD,
		REMOVE,
		CLEAR
	}

	private final NodeGroup group;
	private final Action action;
	private final Collection<String> changedTerms;

	public NodeGroupSearchTermsChangedEvent(NodeGroup group, Action action, Collection<String> terms) {
		this.group = group;
		this.action = action;
		this.changedTerms = new ArrayList<>(terms);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
