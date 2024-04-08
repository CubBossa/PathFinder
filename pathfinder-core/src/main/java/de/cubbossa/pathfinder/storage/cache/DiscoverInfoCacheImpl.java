package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.DiscoverInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DiscoverInfoCacheImpl implements StorageCache<DiscoverInfo>, DiscoverInfoCache {

  private final Map<UUID, Cache<NamespacedKey, DiscoverInfo>> cache;

  public DiscoverInfoCacheImpl() {
    cache = new HashMap<>();
  }

  private Cache<NamespacedKey, DiscoverInfo> playerCache(UUID uuid) {
    return cache.computeIfAbsent(uuid, uuid1 -> {
      return Caffeine.newBuilder()
          .expireAfterAccess(10, TimeUnit.MINUTES)
          .maximumSize(1000)
          .build();
    });
  }

  @Override
  public Optional<DiscoverInfo> getDiscovery(UUID player, NamespacedKey key) {
    return Optional.ofNullable(playerCache(player).asMap().get(key));
  }

  @Override
  public Optional<Collection<DiscoverInfo>> getDiscovery(UUID player) {
    if (!cache.containsKey(player)) {
      return Optional.empty();
    }
    return Optional.of(new HashSet<>(playerCache(player).asMap().values()));
  }

  @Override
  public void write(DiscoverInfo info) {
    playerCache(info.playerId()).put(info.discoverable(), info);
  }

  @Override
  public void invalidate(DiscoverInfo info) {
    if (!cache.containsKey(info.playerId())) {
      return;
    }
    cache.get(info.playerId()).invalidate(info.discoverable());
    if (cache.get(info.playerId()).asMap().size() == 0) {
      cache.remove(info.playerId());
    }
  }

  @Override
  public void invalidate(UUID player) {
    cache.remove(player);
  }

  @Override
  public void invalidateAll() {
    cache.clear();
  }
}
