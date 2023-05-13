package de.cubbossa.pathfinder.events.nodegroup;

import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public class NodeGroupNameChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final SimpleNodeGroup group;
  private final String nameFormat;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
