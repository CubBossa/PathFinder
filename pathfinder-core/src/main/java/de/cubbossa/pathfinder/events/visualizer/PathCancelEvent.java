package de.cubbossa.pathfinder.events.visualizer;

import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PathCancelEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final UUID playerId;
  private final VisualizerPath path;
  private boolean cancelled = false;

  public PathCancelEvent(UUID playerId, VisualizerPath path) {
    this.playerId = playerId;
    this.path = path;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
