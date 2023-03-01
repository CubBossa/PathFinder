package de.cubbossa.pathfinder.hook;

import de.cubbossa.pathfinder.Dependency;
import de.cubbossa.pathfinder.PathPlugin;

public class PlaceholderHookLoader {

  public static Dependency load(PathPlugin plugin) {
    return new PlaceholderHook(plugin);
  }
}
