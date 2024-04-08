package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathfinder.event.PathTargetReachedEvent;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PathTargetFoundEvent extends Event implements PathTargetReachedEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> player;
  private final VisualizerPath<Player> path;

  public PathTargetFoundEvent(PathPlayer<Player> player, VisualizerPath<Player> path) {
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
