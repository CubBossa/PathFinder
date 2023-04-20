package de.cubbossa.pathfinder.events.nodegroup;

import de.cubbossa.pathapi.event.NodeGroupDeleteEvent;
import de.cubbossa.pathapi.group.NodeGroup;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class GroupDeleteEvent extends Event implements NodeGroupDeleteEvent, Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final NodeGroup group;
  @Setter
  private boolean cancelled = false;

  public GroupDeleteEvent(NodeGroup group) {
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
