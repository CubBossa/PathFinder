package de.cubbossa.pathapi.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.DiscoverInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface DiscoverInfoCache extends StorageCache<DiscoverInfo> {

  Optional<DiscoverInfo> getDiscovery(UUID player, NamespacedKey key);

  Optional<Collection<DiscoverInfo>> getDiscovery(UUID player);

  void invalidate(UUID player);
}
