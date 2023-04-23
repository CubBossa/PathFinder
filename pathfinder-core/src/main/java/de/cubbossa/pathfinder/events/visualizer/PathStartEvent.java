package de.cubbossa.pathfinder.events.visualizer;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PathStartEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<?> player;
  private final VisualizerPath<?> path;
  private Location target;
  private float distance;
  private boolean cancelled = false;

  public PathStartEvent(PathPlayer<Player> player, VisualizerPath<?> path, Location target,
                        float distance) {
    this.player = player;
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
