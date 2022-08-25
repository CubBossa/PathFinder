package de.cubbossa.pathfinder.core.events.roadmap;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class RoadmapSelectEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final NamespacedKey current;
	private final NamespacedKey roadMap;
	private boolean cancelled;

	public RoadmapSelectEvent(Player player, NamespacedKey current, NamespacedKey roadMap) {
		this.player = player;
		this.current = current;
		this.roadMap = roadMap;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
