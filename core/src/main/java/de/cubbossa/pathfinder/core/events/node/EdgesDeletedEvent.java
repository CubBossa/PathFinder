package de.cubbossa.pathfinder.core.events.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.Edge;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class EdgesDeletedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Edge> edges;

  public EdgesDeletedEvent(Edge edge) {
    this.edges = Lists.newArrayList(edge);
  }

  public EdgesDeletedEvent(Collection<Edge> edges) {
    this.edges = new ArrayList<>(edges);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
