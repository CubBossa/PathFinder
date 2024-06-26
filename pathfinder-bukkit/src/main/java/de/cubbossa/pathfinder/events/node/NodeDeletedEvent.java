package de.cubbossa.pathfinder.events.node;

import de.cubbossa.pathfinder.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeDeletedEvent extends Event implements NodeDeleteEvent {

  private static final HandlerList handlers = new HandlerList();

  private final Node node;

  public NodeDeletedEvent(Node node) {
    this.node = node;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
