package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathfinder.storage.cache.GroupCache;
import de.cubbossa.pathfinder.storage.cache.NodeCache;
import de.cubbossa.pathfinder.storage.cache.NodeTypeCache;
import de.cubbossa.pathfinder.storage.cache.StorageCache;
import de.cubbossa.pathfinder.storage.cache.VisualizerCache;
import de.cubbossa.pathfinder.storage.cache.VisualizerTypeCache;

public interface CacheLayer extends Iterable<StorageCache<?>> {

  NodeTypeCache getNodeTypeCache();

  NodeCache getNodeCache();

  GroupCache getGroupCache();

  VisualizerTypeCache getVisualizerTypeCache();

  VisualizerCache getVisualizerCache();

  DiscoverInfoCache getDiscoverInfoCache();
}
