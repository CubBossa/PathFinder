package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.core.node.Node;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodesDeleteEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Node<?>> nodes;
  @Setter
  private boolean cancelled;

  public NodesDeleteEvent(Collection<Node<?>> nodes) {
    this.nodes = nodes;
  }

  public NodesDeleteEvent(Node<?> node) {
    this.nodes = List.of(node);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
