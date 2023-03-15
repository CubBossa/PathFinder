package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeGroupDeletedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final NodeGroup group;

  public NodeGroupDeletedEvent(NodeGroup group) {
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
