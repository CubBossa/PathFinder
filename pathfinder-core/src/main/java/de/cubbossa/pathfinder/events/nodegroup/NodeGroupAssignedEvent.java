package de.cubbossa.pathfinder.events.nodegroup;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class NodeGroupAssignedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final Collection<UUID> groupables;
  private final Collection<NamespacedKey> groups;

  public NodeGroupAssignedEvent(UUID groupables, NamespacedKey groups) {
    this(Lists.newArrayList(groupables), List.of(groups));
  }

  public NodeGroupAssignedEvent(Collection<UUID> groupables, Collection<NamespacedKey> groups) {
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
