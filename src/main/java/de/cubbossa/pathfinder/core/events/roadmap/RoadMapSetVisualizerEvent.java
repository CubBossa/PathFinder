package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class RoadMapSetVisualizerEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final RoadMap roadMap;
	private final PathVisualizer<?> visualizer;
	private boolean cancelled;

	public RoadMapSetVisualizerEvent(RoadMap roadMap, PathVisualizer<?> visualizer) {
		this.roadMap = roadMap;
		this.visualizer = visualizer;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
