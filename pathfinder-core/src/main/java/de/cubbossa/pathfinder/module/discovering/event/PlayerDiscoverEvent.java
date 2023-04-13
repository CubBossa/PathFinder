package de.cubbossa.pathfinder.module.discovering.event;

import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerDiscoverEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private final UUID playerId;
  private final NodeGroup group;
  private final LocalDateTime date;
  private boolean cancelled;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
