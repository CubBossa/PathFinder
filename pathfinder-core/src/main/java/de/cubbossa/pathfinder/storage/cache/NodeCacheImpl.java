package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public class NodeCacheImpl implements NodeCache {

  private final Map<UUID, Node> nodeCache;

  private boolean allCached = false;

  public NodeCacheImpl() {
    nodeCache = new HashMap<>();
  }

  @Override
  public <N extends Node> Optional<N> getNode(UUID uuid) {
    return Optional.ofNullable((N) nodeCache.get(uuid));
  }

  @Override
  public Optional<Collection<Node>> getAllNodes() {
    return Optional.ofNullable(allCached ? new HashSet<>(nodeCache.values()) : null);
  }

  @Override
  public CacheCollection<UUID, Node> getNodes(Collection<UUID> ids) {
    if (allCached) {
      return new CacheCollection<>(new HashSet<>(ids.stream()
          .map(nodeCache::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet())), new HashSet<>());
    }
    Collection<Node> result = new HashSet<>();
    Collection<UUID> absent = new HashSet<>();
    for (UUID id : ids) {
      if (!nodeCache.containsKey(id)) {
        absent.add(id);
      } else {
        result.add(nodeCache.get(id));
      }
    }
    return new CacheCollection<>(result, absent);
  }

  @Override
  public void writeAll(Collection<Node> nodes) {
    allCached = true;
    nodes.forEach(this::write);
  }

  @Override
  public void write(NodeGroup group) {

  }

  public void write(Node node) {
    nodeCache.put(node.getNodeId(), node);
  }

  public void invalidate(UUID uuid) {
    nodeCache.remove(uuid);
    nodeCache.values().forEach(node -> {
      node.getEdges().removeIf(edge -> edge.getEnd().equals(uuid));
    });
  }

  public void invalidate(Node node) {
    nodeCache.remove(node.getNodeId());
  }

  @Override
  public void invalidateAll() {
    nodeCache.clear();
    allCached = false;
  }
}
