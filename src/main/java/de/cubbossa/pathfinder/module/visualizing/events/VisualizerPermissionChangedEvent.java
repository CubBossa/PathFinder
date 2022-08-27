package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class VisualizerPermissionChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final PathVisualizer visualizer;
	private final String oldPermission;
	private final String newPermission;

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
