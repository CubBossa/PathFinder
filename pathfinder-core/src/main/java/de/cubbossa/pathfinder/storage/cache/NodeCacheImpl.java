package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.cache.NodeCache;
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

public class NodeCacheImpl implements NodeCache {

  private final Map<UUID, Node<?>> nodeCache;

  private boolean allCached = false;

  public NodeCacheImpl() {
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

  public Collection<Node<?>> getNodes(Collection<UUID> ids,
                                      Function<Collection<UUID>, Collection<? extends Node<?>>> loader) {
    Collection<Node<?>> result = new HashSet<>();
    SortedSet<UUID> sortedIds = new TreeSet<>(ids);

    if (!allCached) {
      Collection<UUID> notPreset = new HashSet<>();
      for (UUID id : ids) {
        if (!nodeCache.containsKey(id)) {
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

  public void write(NodeGroup group, Collection<UUID> deleted) {
    for (UUID uuid : group) {
      if (nodeCache.get(uuid) instanceof Groupable<?> groupable) {
        groupable.addGroup(group);
      }
    }
    for (UUID uuid : deleted) {
      if (nodeCache.get(uuid) instanceof Groupable<?> groupable) {
        groupable.removeGroup(group.getKey());
      }
    }
  }

  public void invalidate(UUID uuid) {
    nodeCache.remove(uuid);
    nodeCache.values().forEach(node -> {
      node.getEdges().removeIf(edge -> edge.getEnd().equals(uuid));
    });
  }

  public void invalidate(Node<?> node) {
    nodeCache.remove(node.getNodeId());
  }

  @Override
  public void invalidateAll() {
    nodeCache.clear();
    allCached = false;
  }
}
