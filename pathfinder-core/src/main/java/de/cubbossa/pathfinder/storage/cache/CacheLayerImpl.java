package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.CacheLayer;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.cache.DiscoverInfoCache;
import de.cubbossa.pathapi.storage.cache.GroupCache;
import de.cubbossa.pathapi.storage.cache.NodeCache;
import de.cubbossa.pathapi.storage.cache.NodeTypeCache;
import de.cubbossa.pathapi.storage.cache.StorageCache;
import de.cubbossa.pathapi.storage.cache.VisualizerCache;
import de.cubbossa.pathapi.storage.cache.VisualizerTypeCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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
    return new CacheLayerImpl(new NodeTypeCache() {
      @Override
      public <N extends Node> NodeType<N> getType(UUID uuid,
                                                  Function<UUID, Optional<NodeType<N>>> loader) {
        return loader.apply(uuid).orElseThrow(
            () -> new RuntimeException("Could not access type of node '" + uuid + "'.")
        );
      }

      @Override
      public Map<UUID, NodeType<?>> getTypes(Collection<UUID> uuids,
                                             Function<Collection<UUID>, Map<UUID, NodeType<?>>> loader) {
        return loader.apply(uuids);
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
          public void write(Node node) {

          }

          @Override
          public void invalidate(Node node) {

          }

          @Override
          public void invalidateAll() {

          }

          @Override
          public Optional<Node> getNode(UUID uuid) {
            return Optional.empty();
          }

          @Override
          public Collection<Node> getAllNodes(Supplier<Collection<Node>> loader) {
            return loader.get();
          }

          @Override
          public Collection<Node> getNodes(Collection<UUID> ids,
                                           Function<Collection<UUID>, Collection<? extends Node>> loader) {
            return (Collection<Node>) loader.apply(ids);
          }

          @Override
          public void write(NodeGroup group, Collection<UUID> deleted) {

          }

          @Override
          public void invalidate(UUID uuid) {

          }
        }, new GroupCache() {
      @Override
      public Optional<NodeGroup> getGroup(NamespacedKey key,
                                          Function<NamespacedKey, NodeGroup> loader) {
        return Optional.ofNullable(loader.apply(key));
      }

      @Override
      public <M extends Modifier> Collection<NodeGroup> getGroups(Class<M> modifier,
                                                                  Function<Class<M>, Collection<NodeGroup>> loader) {
        return loader.apply(modifier);
      }

      @Override
      public List<NodeGroup> getGroups(Pagination pagination,
                                       Function<Pagination, List<NodeGroup>> loader) {
        return loader.apply(pagination);
      }

      @Override
      public Collection<NodeGroup> getGroups(Collection<NamespacedKey> keys,
                                             Function<Collection<NamespacedKey>, Collection<NodeGroup>> loader) {
        return loader.apply(keys);
      }

      @Override
      public Collection<NodeGroup> getGroups(Supplier<Collection<NodeGroup>> loader) {
        return loader.get();
      }

      @Override
      public Collection<NodeGroup> getGroups(UUID node,
                                             Function<UUID, Collection<NodeGroup>> loader) {
        return loader.apply(node);
      }

      @Override
      public void write(Node node) {

      }

      @Override
      public void invalidate(Node node) {

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
    }, new VisualizerCache() {
      @Override
      public <T extends PathVisualizer<?, ?>> Optional<T> getVisualizer(NamespacedKey key,
                                                                        Function<NamespacedKey, T> loader) {
        return Optional.ofNullable(loader.apply(key));
      }

      @Override
      public Collection<PathVisualizer<?, ?>> getVisualizers(
          Supplier<Collection<PathVisualizer<?, ?>>> loader) {
        return loader.get();
      }

      @Override
      public <T extends PathVisualizer<?, ?>> Collection<T> getVisualizers(
          VisualizerType<T> type,
          Function<VisualizerType<T>, Collection<T>> loader) {
        return loader.apply(type);
      }

      @Override
      public void write(PathVisualizer<?, ?> visualizer) {

      }

      @Override
      public void invalidate(PathVisualizer<?, ?> visualizer) {

      }

      @Override
      public void invalidateAll() {

      }
    }, new DiscoverInfoCache() {
      @Override
      public Optional<DiscoverInfo> getDiscovery(UUID player, NamespacedKey key,
                                                 BiFunction<UUID, NamespacedKey, DiscoverInfo> loader) {
        return Optional.ofNullable(loader.apply(player, key));
      }

      @Override
      public Collection<DiscoverInfo> getDiscovery(UUID player) {
        return null;
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
    }, new VisualizerTypeCache() {
      @Override
      public Map<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> key,
                                                            Function<Collection<NamespacedKey>, Map<NamespacedKey, VisualizerType<?>>> loader) {
        return loader.apply(key);
      }

      @Override
      public <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getType(
          NamespacedKey key,
          Function<NamespacedKey, Optional<VisualizerType<VisualizerT>>> loader) {
        return loader.apply(key).orElseThrow();
      }

      @Override
      public <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key,
                                                                   VisualizerType<VisualizerT> type) {

      }

      @Override
      public void write(
          Map.Entry<NamespacedKey, VisualizerType<?>> namespacedKeyVisualizerTypeEntry) {

      }

      @Override
      public void invalidate(
          Map.Entry<NamespacedKey, VisualizerType<?>> namespacedKeyVisualizerTypeEntry) {

      }

      @Override
      public void invalidateAll() {

      }
    });
  }

  @NotNull
  @Override
  public Iterator<StorageCache<?>> iterator() {
    return caches.iterator();
  }
}
