package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.util.NodeSelection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class NodeCurveLengthChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final NodeSelection nodes;
	private final Double curveLength;

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
