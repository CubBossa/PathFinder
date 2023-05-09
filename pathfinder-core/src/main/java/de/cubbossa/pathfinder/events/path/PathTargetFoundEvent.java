package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PathTargetFoundEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> playerId;
  private final VisualizerPath<Player> path;

  public PathTargetFoundEvent(PathPlayer<Player> playerId, VisualizerPath<Player> path) {
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
