package de.cubbossa.pathfinder.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupAssignEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<UUID> groupables;
  private final Collection<NamespacedKey> groups;
  private Collection<UUID> modifiedGroupables;
  private Collection<NamespacedKey> modifiedGroups;
  private boolean cancelled;

  public NodeGroupAssignEvent(UUID groupable, NamespacedKey groups) {
    this(Lists.newArrayList(groupable), List.of(groups));
  }

  public NodeGroupAssignEvent(Collection<UUID> groupables, Collection<NamespacedKey> groups) {
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
