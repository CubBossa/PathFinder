package de.cubbossa.pathfinder.events;

import de.cubbossa.pathfinder.event.PathFinderReloadEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@RequiredArgsConstructor
public class PathFinderReloadEventImpl extends Event implements PathFinderReloadEvent {

  private static final HandlerList handlers = new HandlerList();
  private final boolean config;
  private final boolean locale;

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  @Override
  public boolean reloadsConfig() {
    return config;
  }

  @Override
  public boolean reloadsLocale() {
    return locale;
  }
}
