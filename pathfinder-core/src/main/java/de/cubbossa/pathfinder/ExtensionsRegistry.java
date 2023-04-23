package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ExtensionsRegistry implements de.cubbossa.pathapi.ExtensionsRegistry {

  private final List<PathFinderExtension> extensions;

  public ExtensionsRegistry() {
    extensions = new ArrayList<>();
  }

  @Override
  public List<PathFinderExtension> getExtensions() {
    return new ArrayList<>(extensions);
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
  public void loadExtensions(PathFinder pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onLoad(pathPlugin));
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
