package de.cubbossa.pathfinder.events.node;

import de.cubbossa.pathapi.event.NodeCreateEvent;
import de.cubbossa.pathapi.node.Node;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeCreatedEvent<N extends Node> extends Event implements NodeCreateEvent {

  private static final HandlerList handlers = new HandlerList();
  private final N node;

  public NodeCreatedEvent(N node) {
    this.node = node;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
