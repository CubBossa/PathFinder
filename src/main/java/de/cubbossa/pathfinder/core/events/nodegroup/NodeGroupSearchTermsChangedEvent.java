package de.cubbossa.pathfinder.core.events.nodegroup;

import de.cubbossa.pathfinder.core.node.NodeGroup;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter

public class NodeGroupSearchTermsChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private final NodeGroup group;
  private final Action action;
  private final Collection<String> changedTerms;
  public NodeGroupSearchTermsChangedEvent(NodeGroup group, Action action,
                                          Collection<String> terms) {
    this.group = group;
    this.action = action;
    this.changedTerms = new ArrayList<>(terms);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public enum Action {
    ADD,
    REMOVE,
    CLEAR
  }
}
