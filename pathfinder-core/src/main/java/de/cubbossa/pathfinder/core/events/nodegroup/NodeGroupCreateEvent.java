package de.cubbossa.pathfinder.core.events.nodegroup;

import lombok.Getter;
import lombok.Setter;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeGroupCreateEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final NamespacedKey group;
  @Setter
  private boolean cancelled;

  public NodeGroupCreateEvent(NamespacedKey group) {
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
