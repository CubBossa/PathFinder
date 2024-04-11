package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathfinder.BukkitPathPlayer;
import de.cubbossa.pathfinder.event.PathStoppedEvent;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.GroupedVisualizerPathImpl;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PathStopEvent extends Event implements Cancellable, PathStoppedEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final PathPlayer<Player> player;
  private final GroupedVisualizerPathImpl<Player> path;
  private boolean cancelled = false;

  public PathStopEvent(UUID playerId, GroupedVisualizerPathImpl<Player> path) {
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
