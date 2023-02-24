package de.cubbossa.pathfinder.core.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupAssignedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Groupable<?>> groupables;
  private final Collection<NodeGroup> groups;

  public NodeGroupAssignedEvent(Groupable<?> groupables, NodeGroup groups) {
    this(Lists.newArrayList(groupables), List.of(groups));
  }

  public NodeGroupAssignedEvent(Collection<Groupable<?>> groupables, Collection<NodeGroup> groups) {
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
    return "NodeGroupAssignedEvent{" +
        "groupables=" + groupables +
        ", groups=" + groups +
        '}';
  }
}
