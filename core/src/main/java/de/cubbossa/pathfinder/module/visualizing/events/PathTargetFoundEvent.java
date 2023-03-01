package de.cubbossa.pathfinder.module.visualizing.events;

import de.cubbossa.pathfinder.module.visualizing.VisualizerPath;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PathTargetFoundEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final UUID playerId;
  private final VisualizerPath path;

  public PathTargetFoundEvent(UUID playerId, VisualizerPath path) {
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
