package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.api.event.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.api.event.PathFinderEvent;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import lombok.Getter;
import lombok.Setter;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
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
