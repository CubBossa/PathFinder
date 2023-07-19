package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class ExtensionsRegistry implements de.cubbossa.pathapi.ExtensionsRegistry {

  private final Set<PathFinderExtension> extensions;

  public ExtensionsRegistry() {
    extensions = new HashSet<>();
  }

  @Override
  public Collection<PathFinderExtension> getExtensions() {
    return new HashSet<>(extensions);
  }

  @Override
  public void registerExtension(PathFinderExtension module) {
    extensions.add(module);
  }

  @Override
  public void unregisterExtension(PathFinderExtension module) {
    extensions.remove(module);
  }

  public void findServiceExtensions(ClassLoader classLoader) {
    ServiceLoader<PathFinderExtension> loader =
        ServiceLoader.load(PathFinderExtension.class, classLoader);
    loader.forEach(extensions::add);
  }

  @Override
  public void loadExtensions(PathFinder pathFinder) {
    new ArrayList<>(extensions).forEach(e -> e.onLoad(pathFinder));
  }

  @Override
  public void enableExtensions(PathFinder pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onEnable(pathPlugin));
  }

  @Override
  public void disableExtensions(PathFinder pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onDisable(pathPlugin));
  }
}
