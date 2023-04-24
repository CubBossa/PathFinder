package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.event.NodeDeleteEvent;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Holds an instance of {@link StorageImplementation}. It manages caching and calls the implementation
 * methods where needed.
 */
public interface Storage {

  /**
   * Initializes the storage by initializing the underlying implementation and all caches.
   */
  void init() throws Exception;

  /**
   * Shuts down the storage by shutting down the underlying implementation and invalidating all caches.
   */
  void shutdown();

  /**
   * @return The implementation instance.
   */
  StorageImplementation getImplementation();

  CacheLayer getCache();

  void setCache(CacheLayer cacheLayer);

  /**
   * Loads the node type for a node with given {@link UUID}.
   *
   * @param node The {@link UUID}
   * @param <N>  The Node type.
   * @return The {@link NodeType} instance wrapped in {@link CompletableFuture}.
   */
  <N extends Node<N>> CompletableFuture<Optional<NodeType<N>>> loadNodeType(UUID node);

  /**
   * Loads the node type for multiple nodes by their {@link UUID}s.
   *
   * @param nodes A set of {@link UUID}s to retrieve {@link NodeType}s for.
   * @return A map of all uuids with their found node types. If no type was found, it is not included
   * in the map. Therefore, the size of the return map must not be equal to the size of the input collection.
   */
  CompletableFuture<Map<UUID, NodeType<? extends Node<?>>>> loadNodeTypes(Collection<UUID> nodes);

  // Nodes

  /**
   * Creates a {@link Node} of a given {@link NodeType} asynchronously.
   *
   * @param type     The {@link NodeType} instance that shall be used to create the node.
   * @param location The {@link Location} at which the new node shall be.
   * @param <N>      The Node type.
   * @return A node instance matching the type parameter wrapped in {@link CompletableFuture}.
   */
  <N extends Node<N>> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location);

  /**
   * Loads, modifies and saves a node with given {@link UUID} asynchronously.
   *
   * @param id      The {@link UUID} of the node to edit.
   * @param updater A consumer that will be applied to the requested node once loaded.
   * @return A {@link CompletableFuture} indicating the completion of the process.
   */
  CompletableFuture<Void> modifyNode(UUID id, Consumer<Node<?>> updater);

  <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(UUID id);

  <N extends Node<N>> CompletableFuture<Optional<N>> loadNode(
      NodeType<N> type, UUID id);

  CompletableFuture<Collection<Node<?>>> loadNodes();

  CompletableFuture<Collection<Node<?>>> loadNodes(Collection<UUID> ids);

  CompletableFuture<Void> saveNode(Node<?> node);

  /**
   * A wrapper for {@link #deleteNodes(Collection)} that first resolves the input collection of
   * {@link UUID}s to their according {@link Node}s.
   *
   * @param uuids A collection of {@link UUID}s to delete.
   * @return A {@link CompletableFuture} indicating the completion of the process.
   * @see #deleteNodes(Collection)
   */
  CompletableFuture<Void> deleteNodesById(Collection<UUID> uuids);

  /**
   * Deletes a collection of nodes from storage asynchronously.
   * A call of this method must fire the according {@link NodeDeleteEvent}.
   * After successfull completion, all given {@link Node}s, all according {@link Edge}s,
   * {@link NodeGroup}- and {@link NodeType} mappings must be deleted.
   *
   * @param nodes A collection of nodes to delete.
   * @return A {@link CompletableFuture} indicating the completion of the process.
   */
  CompletableFuture<Void> deleteNodes(Collection<Node<?>> nodes);

  // Groups
  CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key);

  CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Pagination pagination);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Collection<NamespacedKey> keys);

  CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node);

  <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(Class<M> modifier);

  CompletableFuture<Collection<NodeGroup>> loadAllGroups();

  CompletableFuture<Void> saveGroup(NodeGroup group);

  CompletableFuture<Void> deleteGroup(NodeGroup group);

  // Find Data
  CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                            LocalDateTime time);

  CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key);

  CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer
  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
      PathVisualizer<T, ?, ?> visualizer);

  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<T> createAndLoadVisualizer(
      VisualizerType<T> type, NamespacedKey key);

  CompletableFuture<Collection<PathVisualizer<?, ?, ?>>> loadVisualizers();

  <T extends PathVisualizer<T, ?, ?>> CompletableFuture<Map<NamespacedKey, T>> loadVisualizers(
      VisualizerType<T> type);

  <T extends PathVisualizer<T, D, ?>, D> CompletableFuture<Optional<T>> loadVisualizer(
      NamespacedKey key);

  CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?, ?> visualizer);

  CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?, ?> visualizer);
}
