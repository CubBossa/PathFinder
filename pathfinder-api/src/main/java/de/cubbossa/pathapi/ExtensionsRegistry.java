package de.cubbossa.pathapi;

import java.util.Collection;

/**
 * Handles all extensions of one PathFinder application.
 * Extensions are different applications that rely on PathFinders life cycle hooks.
 */
public interface ExtensionsRegistry {

  /**
   * Get all registered extensions.
   *
   * @return An immutable collection of extensions.
   */
  Collection<PathFinderExtension> getExtensions();

  /**
   * Manually registers a new PathFinderExtension.
   * Only call this if you do not make use of the java Service system.
   *
   * @param module the extension instance.
   */
  void registerExtension(PathFinderExtension module);


  /**
   * Manually unregisters a new PathFinderExtension.
   * Only call this if you do not make use of the java Service system.
   *
   * @param module the extension instance.
   */
  void unregisterExtension(PathFinderExtension module);

  /**
   * Loads all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onLoad(PathFinder) 
   * @param pathFinder The current PathFinder instance.
   */
  void loadExtensions(PathFinder pathFinder);

  /**
   * Enables all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onEnable(PathFinder) 
   * @param pathFinder The current PathFinder instance.
   */
  void enableExtensions(PathFinder pathFinder);

  /**
   * Disables all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onDisable(PathFinder)
   * @param pathFinder The current PathFinder instance.
   */
  void disableExtensions(PathFinder pathFinder);
}
