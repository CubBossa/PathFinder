package de.cubbossa.pathfinder.core.events.nodegroup;

import lombok.Getter;
import lombok.Setter;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeGroupDeleteEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final NamespacedKey group;
  @Setter
  private boolean cancelled = false;

  public NodeGroupDeleteEvent(NamespacedKey group) {
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
