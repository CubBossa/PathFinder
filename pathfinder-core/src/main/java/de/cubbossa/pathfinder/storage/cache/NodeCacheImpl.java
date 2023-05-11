package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.cache.NodeCache;

import java.util.*;

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
      return new CacheCollection<>(new HashSet<>(nodeCache.values()), new HashSet<>());
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
    nodes.forEach(node -> nodeCache.put(node.getNodeId(), node));
  }

  public void write(Node node) {
    nodeCache.put(node.getNodeId(), node);
  }

  public void write(NodeGroup group, Collection<UUID> deleted) {
    for (UUID uuid : group) {
      if (nodeCache.get(uuid) instanceof Groupable groupable) {
        groupable.addGroup(group);
      }
    }
    for (UUID uuid : deleted) {
      if (nodeCache.get(uuid) instanceof Groupable groupable) {
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

  public void invalidate(Node node) {
    nodeCache.remove(node.getNodeId());
  }

  @Override
  public void invalidateAll() {
    nodeCache.clear();
    allCached = false;
  }
}
