package de.cubbossa.pathfinder.core.events.node;

import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class  EdgesCreateEvent {

  private EdgesCreateEvent() {}

  @Getter
  public static class Pre extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final NodeSelection from;
    private final NodeSelection to;
    @Setter
    private boolean cancelled = false;

    public Pre(NodeSelection from, NodeSelection to) {
      this.from = from;
      this.to = to;
    }

    public static HandlerList getHandlerList() {
      return handlers;
    }

    public HandlerList getHandlers() {
      return handlers;
    }
  }

  @Getter
  public static class Post extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Collection<Edge> edges;

    public Post(Collection<Edge> edges) {
      this.edges = new HashSet<>(edges);
    }

    public static HandlerList getHandlerList() {
      return handlers;
    }

    public HandlerList getHandlers() {
      return handlers;
    }
  }
}
