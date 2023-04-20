package de.cubbossa.pathfinder.events.node;

import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EdgesDeleteEvent {

  private EdgesDeleteEvent() {}

  @Getter
  @RequiredArgsConstructor
  public static class Pre extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final NodeSelection from;
    private final NodeSelection to;
    @Setter
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
      return handlers;
    }

    public HandlerList getHandlers() {
      return handlers;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class Post extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Collection<SimpleEdge> edges;

    public static HandlerList getHandlerList() {
      return handlers;
    }

    public HandlerList getHandlers() {
      return handlers;
    }
  }
}
