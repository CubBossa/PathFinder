package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.visualizer.CombinedVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

@Getter
@Setter
@RequiredArgsConstructor
public class CombinedVisualizerChangedEvent extends Event {

	public enum Action {ADD, REMOVE, CLEAR}

	private static final HandlerList handlers = new HandlerList();

	private final CombinedVisualizer visualizer;
	private final Action action;
	private final Collection<PathVisualizer<?, ?>> targets;

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
