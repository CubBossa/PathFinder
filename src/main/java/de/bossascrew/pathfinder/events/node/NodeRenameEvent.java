package de.bossascrew.pathfinder.events.node;

import de.bossascrew.pathfinder.node.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeRenameEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Node node;
	private final String newNameFormat;
	private String newNameModified;
	private boolean cancelled = false;

	public NodeRenameEvent(Node node, String newNameFormat) {
		this.node = node;
		this.newNameFormat = newNameFormat;
		this.newNameModified = newNameFormat;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
