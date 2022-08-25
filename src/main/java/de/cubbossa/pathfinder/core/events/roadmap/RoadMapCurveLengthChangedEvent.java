package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class RoadMapCurveLengthChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final RoadMap roadMap;
	private final double oldValue;
	private final double value;

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
