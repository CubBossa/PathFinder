package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.PathFinder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class PathFinderProvider {

  private static PathFinder instance = null;

  public static @NotNull PathFinder get() {
    return instance;
  }

  @ApiStatus.Internal
  public static void setPathFinder(PathFinder pathFinder) {
    instance = pathFinder;
  }
}
