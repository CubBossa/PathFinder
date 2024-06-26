package de.cubbossa.pathfinder.events.discovering;

import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.PathPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerDiscoverProgressEvent extends Event implements de.cubbossa.pathfinder.event.PlayerDiscoverProgressEvent<Player> {

  private static final HandlerList handlers = new HandlerList();
  private final PathPlayer<Player> player;
  private final NodeGroup foundGroup;
  private final NodeGroup progressObserverGroup;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
