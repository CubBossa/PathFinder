package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathapi.storage.cache.GroupCache;
import de.cubbossa.pathapi.storage.cache.NodeCache;
import de.cubbossa.pathapi.storage.cache.NodeTypeCache;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.storage.cache.VisualizerCache;
import de.cubbossa.pathapi.storage.cache.VisualizerTypeCache;

public interface CacheLayer extends Iterable<StorageCache<?>> {

  NodeTypeCache getNodeTypeCache();

  NodeCache getNodeCache();

  GroupCache getGroupCache();

  VisualizerTypeCache getVisualizerTypeCache();

  VisualizerCache getVisualizerCache();

  DiscoverInfoCache getDiscoverInfoCache();
}
