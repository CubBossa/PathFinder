package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.core.node.Node;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class NodeCache {

  private final Map<UUID, Node<?>> nodeCache;

  private boolean allCached = false;

  public NodeCache() {
    nodeCache = new HashMap<>();
  }

  public Optional<Node<?>> getNode(UUID uuid) {
    Node<?> node = nodeCache.get(uuid);
    return node == null ? Optional.empty() : Optional.of(node);
  }

  public Collection<Node<?>> getAllNodes(Supplier<Collection<Node<?>>> loader) {
    if (!allCached) {
      Collection<Node<?>> result = loader.get();
      result.forEach(this::write);
      allCached = true;
    }
    return nodeCache.values();
  }

  public Collection<Node<?>> getNodes(Collection<UUID> ids, Function<Collection<UUID>, Collection<? extends Node<?>>> loader) {
    Collection<Node<?>> result = new HashSet<>();
    SortedSet<UUID> sortedIds = new TreeSet<>(ids);

    if (!allCached) {
      Collection<UUID> notPreset = new HashSet<>();
      for (UUID id : ids) {
        if (nodeCache.containsKey(id)) {
          notPreset.add(id);
        }
      }
      if (notPreset.size() > 0) {
        loader.apply(notPreset).forEach(this::write);
      }
    }
    for (Node<?> value : nodeCache.values()) {
      if (sortedIds.contains(value.getNodeId())) {
        result.add(value);
      }
    }
    return result;
  }

  public void write(Node<?> node) {
    nodeCache.put(node.getNodeId(), node);
  }

  public void invalidate(UUID uuid) {
    nodeCache.remove(uuid);
  }
}
