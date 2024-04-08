package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathfinder.event.PathCancelledEvent;
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
public class PathCancelEvent extends Event implements Cancellable, PathCancelledEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> player;
  private final VisualizerPath<Player> path;
  private boolean cancelled = false;

  public PathCancelEvent(PathPlayer<Player> player, VisualizerPath<Player> path) {
    this.player = player;
    this.path = path;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
