package de.cubbossa.pathfinder.core.events.node;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class NodesDeletedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Node> nodes;

  public NodesDeletedEvent(Node node) {
    this.nodes = Lists.newArrayList(node);
  }

  public NodesDeletedEvent(Collection<Node> nodes) {
    this.nodes = new ArrayList<>(nodes);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
