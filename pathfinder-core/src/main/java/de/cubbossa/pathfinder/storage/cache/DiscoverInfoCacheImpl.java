package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public class DiscoverInfoCacheImpl implements StorageCache<DiscoverInfo>, DiscoverInfoCache {

  private final Cache<Key, DiscoverInfo> cache;

  public DiscoverInfoCacheImpl() {
    cache = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();
  }

  @Override
  public Optional<DiscoverInfo> getDiscovery(UUID player, NamespacedKey key,
                                             BiFunction<UUID, NamespacedKey, DiscoverInfo> loader) {
    return Optional.ofNullable(cache.get(new Key(player, key), k -> loader.apply(k.uuid, k.key)));
  }

  @Override
  public Collection<DiscoverInfo> getDiscovery(UUID player) {
    return cache.asMap().values().stream()
        .filter(info -> info.playerId().equals(player))
        .collect(Collectors.toList());
  }

  @Override
  public void write(DiscoverInfo info) {
    cache.put(new Key(info.playerId(), info.discoverable()), info);
  }

  @Override
  public void invalidate(DiscoverInfo info) {
    cache.invalidate(new Key(info.playerId(), info.discoverable()));
  }

  @Override
  public void invalidate(UUID player) {
    cache.invalidateAll(
        cache.asMap().keySet().stream().filter(key -> key.uuid.equals(player)).toList());
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
  }

  @EqualsAndHashCode
  @RequiredArgsConstructor
  private static class Key {
    private final UUID uuid;
    private final NamespacedKey key;
  }
}