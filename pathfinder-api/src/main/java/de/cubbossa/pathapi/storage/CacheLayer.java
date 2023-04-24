package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathapi.storage.cache.GroupCache;
import de.cubbossa.pathapi.storage.cache.NodeCache;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.storage.cache.VisualizerCache;

public interface CacheLayer extends Iterable<StorageCache<?>> {

  NodeCache getNodeCache();

  GroupCache getGroupCache();

  VisualizerCache getVisualizerCache();

  DiscoverInfoCache getDiscoverInfoCache();
}
