package de.cubbossa.pathfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ExtensionsRegistry {

  private final List<PathPluginExtension> extensions;

  public ExtensionsRegistry() {
    extensions = new ArrayList<>();
  }

  public List<PathPluginExtension> getExtensions() {
    return new ArrayList<>(extensions);
  }

  public void registerExtension(PathPluginExtension module) {
    extensions.add(module);
  }

  public void unregisterExtension(PathPluginExtension module) {
    extensions.remove(module);
  }

  public void findServiceExtensions(ClassLoader classLoader) {
    ServiceLoader<PathPluginExtension> loader = ServiceLoader.load(PathPluginExtension.class, classLoader);
    loader.forEach(extensions::add);
  }

  public void loadExtensions(PathPlugin pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onLoad(pathPlugin));
  }

  public void enableExtensions(PathPlugin pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onEnable(pathPlugin));
  }

  public void disableExtensions(PathPlugin pathPlugin) {
    new ArrayList<>(extensions).forEach(e -> e.onDisable(pathPlugin));
  }
}
