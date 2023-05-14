package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathapi.event.PathStoppedEvent;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathPlayer;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
public class PathStopEvent extends Event implements Cancellable, PathStoppedEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> player;
  private final CommonVisualizerPath<Player> path;
  private boolean cancelled = false;

  public PathStopEvent(UUID playerId, CommonVisualizerPath<Player> path) {
    this.player = new BukkitPathPlayer(playerId);
    this.path = path;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
