package de.cubbossa.pathfinder.event;

public interface PathFinderReloadEvent extends PathFinderEvent {

  boolean reloadsConfig();

  boolean reloadsLocale();
}
