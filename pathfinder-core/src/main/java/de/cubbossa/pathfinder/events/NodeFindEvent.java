package de.cubbossa.pathfinder.events;

import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NodeFindEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  @Getter
  private final UUID playerId;
  @Getter
  @Setter
  private Waypoint findable;
  @Getter
  @Setter
  private Date date;
  @Getter
  @Setter
  private boolean cancelled;

  public NodeFindEvent(UUID playerId, Waypoint findable, Date date) {
    this.playerId = playerId;
    this.findable = findable;
    this.date = date;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
