package de.cubbossa.pathfinder.module.visualizing.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticleVisualizerParticlesChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
