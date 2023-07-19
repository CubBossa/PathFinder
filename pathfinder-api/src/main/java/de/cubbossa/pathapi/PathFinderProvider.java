package de.cubbossa.pathapi;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class PathFinderProvider {

  private static PathFinder instance = null;

  public static @NotNull PathFinder get() {
    return instance;
  }

  @ApiStatus.Internal
  public static void setPathFinder(PathFinder pathFinder) {
    instance = pathFinder;
  }
}
