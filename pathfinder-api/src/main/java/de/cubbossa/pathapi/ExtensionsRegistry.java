package de.cubbossa.pathapi;

import de.cubbossa.disposables.Disposable;
import java.util.Collection;

/**
 * Handles all extensions of one PathFinder application.
 * Extensions are different applications that rely on PathFinders life cycle hooks.
 */
public interface ExtensionsRegistry extends Disposable {

  Collection<PathFinderExtension> getExtensions();

  /**
   * Loads all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onLoad(PathFinder) 
   */
  void loadExtensions();

  /**
   * Enables all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onEnable(PathFinder) 
   */
  void enableExtensions();

  /**
   * Disables all registered extensions with the current PathFinder application as parameter.
   *
   * @see PathFinderExtension#onDisable(PathFinder)
   */
  void disableExtensions();
}
