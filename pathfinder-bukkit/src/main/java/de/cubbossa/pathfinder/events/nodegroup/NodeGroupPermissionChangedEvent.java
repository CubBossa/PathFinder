package de.cubbossa.pathfinder.events.nodegroup;

import de.cubbossa.pathfinder.nodegroup.NodeGroupImpl;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@AllArgsConstructor
public class NodeGroupPermissionChangedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final NodeGroupImpl group;
  private final @Nullable
  String permission;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
