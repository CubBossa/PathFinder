package de.cubbossa.pathfinder.events.nodegroup;

import de.cubbossa.pathfinder.event.NodeGroupSaveEvent;
import de.cubbossa.pathfinder.group.NodeGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public class GroupSaveEvent extends Event implements NodeGroupSaveEvent {

  private static final HandlerList handlers = new HandlerList();

  private final NodeGroup group;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
