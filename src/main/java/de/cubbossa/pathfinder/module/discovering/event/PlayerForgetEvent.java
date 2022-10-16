package de.cubbossa.pathfinder.module.discovering.event;

import de.cubbossa.pathfinder.core.node.Discoverable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerForgetEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final UUID playerId;
	private final Discoverable discoverable;
	private boolean cancelled;

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
