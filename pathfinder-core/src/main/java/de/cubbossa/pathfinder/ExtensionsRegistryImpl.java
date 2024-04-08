package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.util.ExtensionPoint;
import java.util.logging.Level;

public class ExtensionsRegistryImpl extends ExtensionPoint<PathFinderExtension> implements ExtensionsRegistry {

  private final PathFinder pathFinder;

  public ExtensionsRegistryImpl(PathFinder pathFinder) {
    super(PathFinderExtension.class);
    this.pathFinder = pathFinder;
    this.pathFinder.getDisposer().register(pathFinder, this);
  }

  @Override
  public void dispose() {
    disableExtensions();
  }

  @Override
  public void loadExtensions() {
    getExtensions().forEach(e -> {
      if (e.isDisabled()) {
        return;
      }
      try {
        e.onLoad(pathFinder);
        pathFinder.getLogger().log(Level.INFO, "Extension '" + e.getKey() + "' loaded.");
      } catch (Throwable t) {
        pathFinder.getLogger().log(Level.SEVERE, "Error while loading extension '" + e.getKey() + "'. Skipping extension", t);
      }
    });
  }

  @Override
  public void enableExtensions() {
    getExtensions().forEach(e -> {
      if (e.isDisabled()) {
        return;
      }
      try {
        e.onEnable(pathFinder);
        pathFinder.getLogger().log(Level.INFO, "Extension '" + e.getKey() + "' enabled.");
      } catch (Throwable t) {
        pathFinder.getLogger().log(Level.SEVERE, "Error while enabling extension '" + e.getKey() + "'. Skipping extension", t);
      }
    });
  }

  @Override
  public void disableExtensions() {
    getExtensions().forEach(e -> {
      if (e.isDisabled()) {
        return;
      }
      try {
        e.onDisable(pathFinder);
        pathFinder.getLogger().log(Level.INFO, "Extension '" + e.getKey() + "' disabled.");
      } catch (Throwable t) {
        pathFinder.getLogger().log(Level.SEVERE, "Error while disabling extension '" + e.getKey() + "'.", t);
      }
    });
  }
}
