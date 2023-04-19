package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public class NodeGroupDiscoverableChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final SimpleNodeGroup group;
  private final boolean newValue;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
