package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@AllArgsConstructor
public class NodeGroupFindDistanceChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final SimpleNodeGroup group;
  private final float value;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
