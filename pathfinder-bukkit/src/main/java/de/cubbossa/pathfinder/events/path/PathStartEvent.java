package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PathStartEvent extends Event implements Cancellable, de.cubbossa.pathfinder.event.PathStartEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> player;
  private final VisualizerPath<Player> path;
  private Location target;
  private float distance;
  private boolean cancelled = false;

  public PathStartEvent(PathPlayer<Player> player, VisualizerPath<Player> path, Location target,
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
