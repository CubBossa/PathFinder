package de.cubbossa.pathfinder.events.discovering;

import de.cubbossa.pathapi.event.PlayerForgetLocationEvent;
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

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerForgetEvent extends Event implements Cancellable, PlayerForgetLocationEvent<Player> {

  private static final HandlerList handlers = new HandlerList();
  private final PathPlayer<Player> player;
  private final NodeGroup group;
  private final DiscoverableModifier modifier;
  private boolean cancelled;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
