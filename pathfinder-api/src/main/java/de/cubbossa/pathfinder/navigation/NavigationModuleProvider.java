package de.cubbossa.pathfinder.navigation;

public class NavigationModuleProvider {

  private static NavigationModule<?> module;

  public static <PlayerT> NavigationModule<PlayerT> get() {
    if (module == null) {
      throw new IllegalStateException("Accessing NavigationModule before initialization.");
    }
    return (NavigationModule<PlayerT>) module;
  }

  public static void set(NavigationModule<?> instance) {
    module = instance;
  }

}
