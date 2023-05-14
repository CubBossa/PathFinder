package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.cache.GroupCache;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathfinder.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GroupCacheImpl implements StorageCache<NodeGroup>, GroupCache {

  private final Cache<NamespacedKey, NodeGroup> cache;
  private final Cache<UUID, Collection<NodeGroup>> nodeGroupCache;
  private final Cache<Class<Modifier>, Collection<NodeGroup>> modifierGroupCache;
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
  public <M extends Modifier> Optional<Collection<NodeGroup>> getGroups(Class<M> modifier) {
    if (modifierGroupCache.asMap().containsKey(modifier)) {
      return Optional.of(modifierGroupCache.asMap().get(modifier)).map(HashSet::new);
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
  public Optional<Collection<NodeGroup>> getGroups(Pagination pagination) {
    if (!cachedAll) {
      return Optional.empty();
    }
    return Optional.of(CollectionUtils.subList(cache.asMap().values().stream().toList(), pagination.getOffset(),
        pagination.getOffset() + pagination.getLimit()));
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
    return Optional.ofNullable(nodeGroupCache.asMap().get(node)).map(HashSet::new);
  }

  public void write(NodeGroup group) {
    cache.put(group.getKey(), group);
  }

  @Override
  public void write(Node node) {
    if (node instanceof Groupable groupable) {
      nodeGroupCache.put(node.getNodeId(), Set.copyOf(groupable.getGroups()));
      for (NodeGroup present : cache.asMap().values()) {
        if (groupable.getGroups().contains(present)) {
          present.add(groupable.getNodeId());
        } else {
          present.remove(groupable.getNodeId());
        }
      }
    }
  }

  @Override
  public <M extends Modifier> void write(Class<M> modifier, Collection<NodeGroup> groups) {
    modifierGroupCache.put((Class<Modifier>) modifier, groups);
  }

  @Override
  public void write(UUID node, Collection<NodeGroup> groups) {
    nodeGroupCache.put(node, groups);
  }

  @Override
  public void writeAll(Collection<NodeGroup> groups) {
    groups.forEach(group -> cache.put(group.getKey(), group));
    cachedAll = true;
  }

  public void invalidate(NodeGroup group) {
    cache.invalidate(group.getKey());
    for (UUID nodeId : group) {
      Collection<NodeGroup> groups = nodeGroupCache.getIfPresent(nodeId);
      if (groups != null) {
        groups.remove(group);
      }
    }
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
    cachedAll = false;
    nodeGroupCache.invalidateAll();
  }

  @Override
  public void invalidate(Node node) {
    nodeGroupCache.invalidate(node.getNodeId());
    if (node instanceof Groupable groupable) {
      for (NodeGroup value : cache.asMap().values()) {
        value.remove(groupable.getNodeId());
      }
    }
  }
}
