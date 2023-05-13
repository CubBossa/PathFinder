package de.cubbossa.pathfinder.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupRemoveEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<Groupable> groupables;
  private final Collection<SimpleNodeGroup> groups;
  private Collection<Groupable> modifiedGroupables;
  private Collection<SimpleNodeGroup> modifiedGroups;
  private boolean cancelled;

  public NodeGroupRemoveEvent(Groupable groupables, SimpleNodeGroup groups) {
    this(Lists.newArrayList(groupables), List.of(groups));
  }

  public NodeGroupRemoveEvent(Collection<Groupable> groupables,
                              Collection<SimpleNodeGroup> groups) {
    this.groupables = Collections.unmodifiableCollection(groupables);
    this.groups = Collections.unmodifiableCollection(groups);
    this.modifiedGroupables = new ArrayList<>(groupables);
    this.modifiedGroups = new ArrayList<>(groups);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
