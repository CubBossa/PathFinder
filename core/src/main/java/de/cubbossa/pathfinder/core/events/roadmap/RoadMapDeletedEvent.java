package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class RoadMapDeletedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final RoadMap roadMap;

  public RoadMapDeletedEvent(RoadMap roadMap) {
    this.roadMap = roadMap;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
