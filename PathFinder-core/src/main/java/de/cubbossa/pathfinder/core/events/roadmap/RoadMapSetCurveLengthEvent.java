package de.cubbossa.pathfinder.core.events.roadmap;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class RoadMapSetCurveLengthEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final RoadMap roadMap;
  private final double value;
  private boolean cancelled;

  public RoadMapSetCurveLengthEvent(RoadMap roadMap, double value) {
    this.roadMap = roadMap;
    this.value = value;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
