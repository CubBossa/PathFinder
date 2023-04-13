package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.util.Pagination;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;

public class GroupCache {

  private final Cache<NamespacedKey, NodeGroup> cache;
  private final Cache<UUID, Collection<NodeGroup>> nodeGroupCache;
  private boolean cachedAll = false;

  public GroupCache() {
    this.cache = Caffeine.newBuilder()
        .maximumSize(100)
        .weakValues()
        .build();
    this.nodeGroupCache = Caffeine.newBuilder()
        .weakValues()
        .build();
  }

  public Optional<NodeGroup> getGroup(NamespacedKey key,
                                      Function<NamespacedKey, NodeGroup> loader) {
    return Optional.ofNullable(cache.get(key, loader));
  }

  public <M extends Modifier> Collection<NodeGroup> getGroups(Class<M> modifier, Function<Class<M>, Collection<NodeGroup>> loader) {
    return cachedAll
        ? cache.asMap().values().stream().filter(group -> group.hasModifier(modifier)).toList()
        : loader.apply(modifier);
  }

  public List<NodeGroup> getGroups(Pagination pagination, Function<Pagination, List<NodeGroup>> loader) {
    if (cachedAll) {
      return cache.asMap().values().stream().toList().subList(pagination.getOffset(), pagination.getOffset() + pagination.getLimit());
    }
    return loader.apply(pagination);
  }

  public Collection<NodeGroup> getGroups(Collection<NamespacedKey> keys,
                                         Function<Collection<NamespacedKey>, Collection<NodeGroup>> loader) {
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
    result.addAll(loader.apply(toLoad));
    return result;
  }

  public Collection<NodeGroup> getGroups(Supplier<Collection<NodeGroup>> loader) {
    if (cachedAll) {
      return cache.asMap().values();
    }
    Collection<NodeGroup> loaded = loader.get();
    loaded.forEach(g -> cache.put(g.getKey(), g));
    return loaded;
  }

  public Collection<NodeGroup> getGroups(UUID node, Function<UUID, Collection<NodeGroup>> loader) {
    return nodeGroupCache.get(node, loader);
  }

  public void write(NodeGroup group) {
    cache.put(group.getKey(), group);
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
}
