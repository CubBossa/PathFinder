package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.cache.*;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class CacheLayerImpl implements CacheLayer {

  private final NodeTypeCache nodeTypeCache;
  private final NodeCache nodeCache;
  private final GroupCache groupCache;
  private final VisualizerCache visualizerCache;
  private final DiscoverInfoCache discoverInfoCache;
  private final VisualizerTypeCache visualizerTypeCache;
  private final Collection<StorageCache<?>> caches;

  public CacheLayerImpl() {
    this(new NodeTypeCacheImpl(), new NodeCacheImpl(), new GroupCacheImpl(),
        new VisualizerCacheImpl(), new DiscoverInfoCacheImpl(), new VisualizerTypeCacheImpl());
  }

  public CacheLayerImpl(NodeTypeCache nodeTypeCache, NodeCache nodeCache, GroupCache groupCache,
                        VisualizerCache visualizerCache,
                        DiscoverInfoCache discoverInfoCache,
                        VisualizerTypeCache visualizerTypeCache) {
    this.nodeTypeCache = nodeTypeCache;
    this.nodeCache = nodeCache;
    this.groupCache = groupCache;
    this.visualizerCache = visualizerCache;
    this.discoverInfoCache = discoverInfoCache;
    this.visualizerTypeCache = visualizerTypeCache;
    caches = List.of(nodeCache, nodeTypeCache, groupCache, visualizerCache, visualizerCache,
        visualizerTypeCache);
  }

  public static CacheLayer empty() {
    return new CacheLayerImpl(
        new NodeTypeCache() {
          @Override
          public <N extends Node> Optional<NodeType<N>> getType(UUID uuid) {
            return Optional.empty();
          }

          @Override
          public CacheMap<UUID, NodeType<?>> getTypes(Collection<UUID> uuids) {
            return CacheMap.empty(uuids);
          }

          @Override
          public void write(UUID uuid, NodeType<?> type) {

          }

          @Override
          public void write(UUID uuid) {

          }

          @Override
          public void invalidate(UUID uuid) {

          }

          @Override
          public void invalidateAll() {

          }
        },
        new NodeCache() {
          @Override
          public <N extends Node> Optional<N> getNode(UUID uuid) {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<Node>> getAllNodes() {
            return Optional.empty();
          }

          @Override
          public CacheCollection<UUID, Node> getNodes(Collection<UUID> ids) {
            return CacheCollection.empty(ids);
          }

          @Override
          public void writeAll(Collection<Node> nodes) {

          }

          @Override
          public void write(NodeGroup group) {

          }

          @Override
          public void invalidate(UUID uuid) {

          }

          @Override
          public void write(Node node) {

          }

          @Override
          public void invalidate(Node node) {

          }

          @Override
          public void invalidateAll() {

          }
        },
        new GroupCache() {
          @Override
          public Optional<NodeGroup> getGroup(NamespacedKey key) {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<NodeGroup>> getGroups(NamespacedKey modifier) {
            return Optional.empty();
          }

          @Override
          public CacheCollection<NamespacedKey, NodeGroup> getGroups(Collection<NamespacedKey> keys) {
            return CacheCollection.empty(keys);
          }

          @Override
          public Optional<Collection<NodeGroup>> getGroups() {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<NodeGroup>> getGroups(UUID node) {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<NodeGroup>> getGroups(Range range) {
            return Optional.empty();
          }

          @Override
          public void write(UUID node, Collection<NodeGroup> groups) {

          }

          @Override
          public void write(NamespacedKey modifier, Collection<NodeGroup> groups) {

          }

          @Override
          public void writeAll(Collection<NodeGroup> groups) {

          }

          @Override
          public void invalidate(UUID node) {

          }

          @Override
          public void invalidate(NamespacedKey modifier) {

          }

          @Override
          public void write(NodeGroup group) {

          }

          @Override
          public void invalidate(NodeGroup group) {

          }

          @Override
          public void invalidateAll() {

          }
        },
        new VisualizerCache() {
          @Override
          public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> getVisualizer(NamespacedKey key) {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<PathVisualizer<?, ?>>> getVisualizers() {
            return Optional.empty();
          }

          @Override
          public <VisualizerT extends PathVisualizer<?, ?>> Optional<Collection<VisualizerT>> getVisualizers(VisualizerType<VisualizerT> type) {
            return Optional.empty();
          }

          @Override
          public <VisualizerT extends PathVisualizer<?, ?>> void writeAll(VisualizerType<VisualizerT> type, Collection<VisualizerT> v) {

          }

          @Override
          public void writeAll(Collection<PathVisualizer<?, ?>> visualizers) {

          }

          @Override
          public void write(PathVisualizer<?, ?> pathVisualizer) {

          }

          @Override
          public void invalidate(PathVisualizer<?, ?> pathVisualizer) {

          }

          @Override
          public void invalidateAll() {

          }
        },
        new DiscoverInfoCache() {
          @Override
          public Optional<DiscoverInfo> getDiscovery(UUID player, NamespacedKey key) {
            return Optional.empty();
          }

          @Override
          public Optional<Collection<DiscoverInfo>> getDiscovery(UUID player) {
            return Optional.empty();
          }

          @Override
          public void invalidate(UUID player) {

          }

          @Override
          public void write(DiscoverInfo discoverInfo) {

          }

          @Override
          public void invalidate(DiscoverInfo discoverInfo) {

          }

          @Override
          public void invalidateAll() {

          }
        },
        new VisualizerTypeCache() {
          @Override
          public CacheMap<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> keys) {
            return CacheMap.empty(keys);
          }

          @Override
          public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(NamespacedKey key) {
            return Optional.empty();
          }

          @Override
          public <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key, VisualizerType<VisualizerT> type) {

          }

          @Override
          public void write(Map.Entry<NamespacedKey, VisualizerType<?>> namespacedKeyVisualizerTypeEntry) {

          }

          @Override
          public void invalidate(Map.Entry<NamespacedKey, VisualizerType<?>> namespacedKeyVisualizerTypeEntry) {

          }

          @Override
          public void invalidateAll() {

          }
        }
    );
  }

  @NotNull
  @Override
  public Iterator<StorageCache<?>> iterator() {
    return caches.iterator();
  }
}
