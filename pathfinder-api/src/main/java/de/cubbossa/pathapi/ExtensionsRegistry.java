package de.cubbossa.pathapi;

import java.util.List;

public interface ExtensionsRegistry {
  List<PathFinderExtension> getExtensions();

  void registerExtension(PathFinderExtension module);

  void unregisterExtension(PathFinderExtension module);

  void loadExtensions(PathFinder pathPlugin);

  void enableExtensions(PathFinder pathPlugin);

  void disableExtensions(PathFinder pathPlugin);
}
