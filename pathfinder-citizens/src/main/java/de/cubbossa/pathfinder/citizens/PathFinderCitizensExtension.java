package de.cubbossa.pathfinder.citizens;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class PathFinderCitizensExtension extends PathFinderExtensionBase {

  private static final NamespacedKey KEY = NamespacedKey.fromString("pathfinder:citizens");

  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void onEnable(PathFinder pathFinder) {
    Plugin citizens = Bukkit.getPluginManager().getPlugin("Citizens");
    if (citizens == null) {
      disable();
    }
  }

  @Override
  public void onDisable(PathFinder pathFinder) {
  }
}
