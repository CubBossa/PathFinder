package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class RoadMapCreatedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final RoadMap roadMap;

	public RoadMapCreatedEvent(RoadMap roadMap) {
		this.roadMap = roadMap;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
