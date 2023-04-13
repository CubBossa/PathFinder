package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.api.node.Node;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodesDeletedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Node<?>> nodes;

  public NodesDeletedEvent(Collection<Node<?>> nodes) {
    this.nodes = nodes;
  }

  public NodesDeletedEvent(Node<?> node) {
    this.nodes = List.of(node);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
