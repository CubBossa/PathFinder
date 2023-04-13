package de.cubbossa.pathfinder.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
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

  private final Cache<NamespacedKey, SimpleNodeGroup> cache;
  private final Cache<UUID, Collection<SimpleNodeGroup>> nodeGroupCache;
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

  public Optional<SimpleNodeGroup> getGroup(NamespacedKey key,
                                            Function<NamespacedKey, SimpleNodeGroup> loader) {
    return Optional.ofNullable(cache.get(key, loader));
  }

  public <M extends Modifier> Collection<SimpleNodeGroup> getGroups(Class<M> modifier, Function<Class<M>, Collection<SimpleNodeGroup>> loader) {
    return cachedAll
        ? cache.asMap().values().stream().filter(group -> group.hasModifier(modifier)).toList()
        : loader.apply(modifier);
  }

  public List<SimpleNodeGroup> getGroups(Pagination pagination, Function<Pagination, List<SimpleNodeGroup>> loader) {
    if (cachedAll) {
      return cache.asMap().values().stream().toList().subList(pagination.getOffset(), pagination.getOffset() + pagination.getLimit());
    }
    return loader.apply(pagination);
  }

  public Collection<SimpleNodeGroup> getGroups(Collection<NamespacedKey> keys,
                                               Function<Collection<NamespacedKey>, Collection<SimpleNodeGroup>> loader) {
    Collection<SimpleNodeGroup> result = new HashSet<>();
    Collection<NamespacedKey> toLoad = new HashSet<>();
    for (NamespacedKey key : keys) {
      SimpleNodeGroup group = cache.getIfPresent(key);
      if (group != null) {
        result.add(group);
      } else {
        toLoad.add(key);
      }
    }
    result.addAll(loader.apply(toLoad));
    return result;
  }

  public Collection<SimpleNodeGroup> getGroups(Supplier<Collection<SimpleNodeGroup>> loader) {
    if (cachedAll) {
      return cache.asMap().values();
    }
    Collection<SimpleNodeGroup> loaded = loader.get();
    loaded.forEach(g -> cache.put(g.getKey(), g));
    return loaded;
  }

  public Collection<SimpleNodeGroup> getGroups(UUID node, Function<UUID, Collection<SimpleNodeGroup>> loader) {
    return nodeGroupCache.get(node, loader);
  }

  public void write(SimpleNodeGroup group) {
    cache.put(group.getKey(), group);
  }

  public void invalidate(SimpleNodeGroup group) {
    cache.invalidate(group.getKey());
    for (UUID nodeId : group) {
      Collection<SimpleNodeGroup> groups = nodeGroupCache.getIfPresent(nodeId);
      if (groups != null) {
        groups.remove(group);
      }
    }
  }
}
