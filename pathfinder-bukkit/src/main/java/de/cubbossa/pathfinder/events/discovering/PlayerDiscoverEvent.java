package de.cubbossa.pathfinder.events.discovering;

import de.cubbossa.pathapi.event.PlayerDiscoverLocationEvent;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerDiscoverEvent extends Event implements Cancellable, PlayerDiscoverLocationEvent<Player> {

  private static final HandlerList handlers = new HandlerList();
  private final PathPlayer<Player> player;
  private final NodeGroup group;
  private final DiscoverableModifier modifier;
  private final LocalDateTime timeStamp;
  private boolean cancelled;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
