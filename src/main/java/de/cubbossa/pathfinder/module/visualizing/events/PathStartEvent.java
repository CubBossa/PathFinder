package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.VisualizerPath;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PathStartEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final UUID playerId;
  private final VisualizerPath path;
  private Location target;
  private float distance;
  private boolean cancelled = false;

  public PathStartEvent(UUID playerId, VisualizerPath path, Location target, float distance) {
    this.playerId = playerId;
    this.path = path;
    this.target = target;
    this.distance = distance;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
