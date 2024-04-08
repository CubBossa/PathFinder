package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.Range;
import de.cubbossa.pathfinder.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupCacheImpl implements StorageCache<NodeGroup>, GroupCache {

  private final Cache<NamespacedKey, NodeGroup> cache;
  private final Cache<UUID, Collection<NamespacedKey>> nodeGroupCache;
  private final Cache<NamespacedKey, Collection<NamespacedKey>> modifierGroupCache;
  private boolean cachedAll = false;

  public GroupCacheImpl() {
    this.cache = Caffeine.newBuilder()
        .maximumSize(100)
        .build();
    this.nodeGroupCache = Caffeine.newBuilder()
        .build();
    this.modifierGroupCache = Caffeine.newBuilder()
        .build();
  }

  @Override
  public Optional<NodeGroup> getGroup(NamespacedKey key) {
    return Optional.ofNullable(cache.get(key, key1 -> null));
  }

  @Override
  public Optional<Collection<NodeGroup>> getGroups(NamespacedKey modifier) {
    if (modifierGroupCache.asMap().containsKey(modifier)) {
      return Optional.of(modifierGroupCache.asMap().get(modifier)).map(HashSet::new).map(this::collect);
    }
    if (cachedAll) {
      Collection<NodeGroup> newCache = cache.asMap().values().stream()
          .filter(group -> group.hasModifier(modifier))
          .collect(Collectors.toSet());
      write(modifier, newCache);
      return Optional.of(newCache);
    }
    return Optional.empty();
  }

  @Override
  public Optional<Collection<NodeGroup>> getGroups(Range range) {
    if (!cachedAll) {
      return Optional.empty();
    }
    return Optional.of(CollectionUtils.subList(cache.asMap().values().stream().toList(), range));
  }

  @Override
  public CacheCollection<NamespacedKey, NodeGroup> getGroups(Collection<NamespacedKey> keys) {
    Collection<NodeGroup> result = new HashSet<>();
    Collection<NamespacedKey> toLoad = new HashSet<>();
    for (NamespacedKey key : keys) {
      NodeGroup group = cache.getIfPresent(key);
      if (group != null) {
        result.add(group);
      } else {
        toLoad.add(key);
      }
    }
    return new CacheCollection<>(result, toLoad);
  }

  @Override
  public Optional<Collection<NodeGroup>> getGroups() {
    if (cachedAll) {
      return Optional.of(new HashSet<>(cache.asMap().values()));
    }
    return Optional.empty();
  }

  @Override
  public Optional<Collection<NodeGroup>> getGroups(UUID node) {
    return Optional.ofNullable(nodeGroupCache.asMap().get(node)).map(HashSet::new).map(this::collect);
  }

  private Collection<NodeGroup> collect(Collection<NamespacedKey> keys) {
    return keys.stream()
        .filter(nk -> cache.asMap().containsKey(nk))
        .map(nk -> cache.asMap().get(nk))
        .collect(Collectors.toList());
  }

  public void write(NodeGroup group) {
    cache.put(group.getKey(), group);
  }

  @Override
  public void write(NamespacedKey modifier, Collection<NodeGroup> groups) {
    groups.forEach(this::write);
    modifierGroupCache.put(modifier, groups.stream().map(NodeGroup::getKey).collect(Collectors.toList()));
  }

  @Override
  public void write(UUID node, Collection<NodeGroup> groups) {
    groups.forEach(this::write);
    nodeGroupCache.put(node, groups.stream().map(NodeGroup::getKey).collect(Collectors.toList()));
  }

  @Override
  public void writeAll(Collection<NodeGroup> groups) {
    groups.forEach(this::write);
    cachedAll = true;
  }

  @Override
  public void invalidate(UUID node) {
    nodeGroupCache.invalidate(node);
  }

  @Override
  public void invalidate(NamespacedKey modifier) {
    modifierGroupCache.invalidate(modifier);
  }

  public void invalidate(NodeGroup group) {
    cache.invalidate(group.getKey());
    for (UUID nodeId : group) {
      nodeGroupCache.asMap().computeIfAbsent(nodeId, uuid -> new HashSet<>()).remove(group.getKey());
    }
    for (Modifier modifier : group.getModifiers()) {
      modifierGroupCache.asMap().computeIfAbsent(modifier.getKey(), uuid -> new HashSet<>()).remove(group.getKey());
    }
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
    cachedAll = false;
    nodeGroupCache.invalidateAll();
    modifierGroupCache.invalidateAll();
  }
}
