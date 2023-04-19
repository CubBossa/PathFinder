package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.api.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.api.event.NodeSaveEvent;
import de.cubbossa.pathfinder.api.node.Node;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodeSavedEvent extends Event implements NodeSaveEvent {

  private static final HandlerList handlers = new HandlerList();

  private final Node<?> node;

  public NodeSavedEvent(Node<?> node) {
    this.node = node;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
