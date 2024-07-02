package de.cubbossa.pathfinder.command;

import org.pf4j.ExtensionPoint;

public interface PathFinderReloadListener extends ExtensionPoint {

  default void onReload() {
    onReloadConfig();
    onReloadLocale();
  }

  default void onReloadLocale() {
  }

  default void onReloadConfig() {
  }
}
