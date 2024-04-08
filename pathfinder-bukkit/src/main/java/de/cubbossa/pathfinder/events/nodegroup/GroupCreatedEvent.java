package de.cubbossa.pathfinder.events.nodegroup;

import de.cubbossa.pathfinder.event.NodeGroupCreateEvent;
import de.cubbossa.pathfinder.group.NodeGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GroupCreatedEvent extends Event implements NodeGroupCreateEvent {

  private static final HandlerList handlers = new HandlerList();

  @Getter
  private final NodeGroup group;

  public GroupCreatedEvent(NodeGroup group) {
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
