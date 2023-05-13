package de.cubbossa.pathfinder.events.node;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class NodeCreateEvent<N extends Node> extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private final NodeType<N> nodeType;
  private Location location;
  private boolean cancelled;

  public NodeCreateEvent(NodeType<N> type, Location location) {
    this.nodeType = type;
    this.location = location;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
