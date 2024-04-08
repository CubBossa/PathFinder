package de.cubbossa.pathfinder.events.node;

import de.cubbossa.pathfinder.node.NodeSelection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeTeleportEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final NodeSelection nodes;
  private final Location newPosition;
  private Location newPositionModified;
  private boolean cancelled = false;

  public NodeTeleportEvent(NodeSelection nodes, Location newPosition) {
    this.nodes = nodes;
    this.newPosition = newPosition;
    this.newPositionModified = newPosition;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

}
