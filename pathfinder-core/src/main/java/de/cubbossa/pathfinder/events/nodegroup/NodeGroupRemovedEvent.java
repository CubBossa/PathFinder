package de.cubbossa.pathfinder.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
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

  private final Collection<Groupable<?>> groupables;
  private final Collection<SimpleNodeGroup> groups;

  public NodeGroupRemovedEvent(Groupable<?> groupables, SimpleNodeGroup groups) {
    this(Lists.newArrayList(groupables), List.of(groups));
  }

  public NodeGroupRemovedEvent(Collection<Groupable<?>> groupables,
                               Collection<SimpleNodeGroup> groups) {
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
