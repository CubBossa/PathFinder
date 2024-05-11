package de.cubbossa.pathfinder.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.nodegroup.NodeGroupImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupRemovedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Node> groupables;
  private final Collection<NodeGroupImpl> groups;

  public NodeGroupRemovedEvent(Node groupables, NodeGroupImpl groups) {
    this(Lists.newArrayList(groupables), List.of(groups));
  }

  public NodeGroupRemovedEvent(Collection<Node> groupables,
                               Collection<NodeGroupImpl> groups) {
    this.groupables = Collections.unmodifiableCollection(groupables);
    this.groups = Collections.unmodifiableCollection(groups);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  @Override
  public String toString() {
    return "NodeGroupRemovedEvent{" +
        "groupables=" + groupables +
        ", groups=" + groups +
        '}';
  }
}
