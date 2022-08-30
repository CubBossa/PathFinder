package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class RoadMapSetNameEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final RoadMap roadMap;
	private final String nameFormat;
	private boolean cancelled;

	public RoadMapSetNameEvent(RoadMap roadMap, String nameFormat) {
		this.roadMap = roadMap;
		this.nameFormat = nameFormat;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
