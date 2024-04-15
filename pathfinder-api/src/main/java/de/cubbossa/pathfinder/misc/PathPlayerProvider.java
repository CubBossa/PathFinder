package de.cubbossa.pathfinder.misc;

import java.util.UUID;

public abstract class PathPlayerProvider<P> {

  private static PathPlayerProvider<?> instance;

  public static <P> PathPlayerProvider<P> get() {
    return (PathPlayerProvider<P>) instance;
  }

  public static void set(PathPlayerProvider<?> provider) {
    instance = provider;
  }

  public abstract PathPlayer<? extends P> wrap(P player);

  public abstract PathPlayer<P> wrap(UUID uuid);

  public abstract PathPlayer<P> consoleSender();
}
