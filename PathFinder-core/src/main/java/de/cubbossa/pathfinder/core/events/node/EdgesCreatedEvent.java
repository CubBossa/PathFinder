package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.core.node.Edge;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class EdgesCreatedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Edge> edges;

  public EdgesCreatedEvent(Edge... edges) {
    this.edges = new HashSet<>();
    this.edges.addAll(List.of(edges));
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
