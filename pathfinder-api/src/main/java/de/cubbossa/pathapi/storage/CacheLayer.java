package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.storage.cache.*;

public interface CacheLayer extends Iterable<StorageCache<?>> {

		NodeCache getNodeCache();

		GroupCache getGroupCache();

		VisualizerCache getVisualizerCache();

		DiscoverInfoCache getDiscoverInfoCache();
}
