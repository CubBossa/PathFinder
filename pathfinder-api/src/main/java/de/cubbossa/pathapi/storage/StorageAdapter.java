package de.cubbossa.pathapi.storage;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.event.NodeDeleteEvent;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;
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
public interface StorageAdapter extends Disposable {

  /**
   * Initializes the storage by initializing the underlying implementation and all caches.
   */
  void init() throws Exception;

  /**
   * Shuts down the storage by shutting down the underlying implementation and invalidating all caches.
   */
  void shutdown();

  EventDispatcher<?> getEventDispatcher();

  void setEventDispatcher(EventDispatcher<?> eventDispatcher);

  /**
   * @return The implementation instance.
   */
  StorageImplementation getImplementation();

  CacheLayer getCache();

  void setCache(CacheLayer cacheLayer);


  /**
   * Check if global group exists and if not create.
   * Global group will use default visualizer, so if default visualizer has been deleted, it will be recreated.
   */
  CompletableFuture<NodeGroup> createGlobalNodeGroup(VisualizerType<?> defaultVisualizerType);

  /**
   * Loads the node type for a node with given {@link UUID}.
   *
   * @param node The {@link UUID}
   * @param <N>  The Node type.
   * @return The {@link NodeType} instance wrapped in {@link CompletableFuture}.
   */
  <N extends Node> CompletableFuture<Optional<NodeType<N>>> loadNodeType(UUID node);

  /**
   * Loads the node type for multiple nodes by their {@link UUID}s.
   *
   * @param nodes A set of {@link UUID}s to retrieve {@link NodeType}s for.
   * @return A map of all uuids with their found node types. If no type was found, it is not included
   * in the map. Therefore, the size of the return map must not be equal to the size of the input collection.
   */
  CompletableFuture<Map<UUID, NodeType<?>>> loadNodeTypes(Collection<UUID> nodes);

  // Nodes

  /**
   * Creates a {@link Node} of a given {@link NodeType} asynchronously.
   *
   * @param type     The {@link NodeType} instance that shall be used to create the node.
   * @param location The {@link Location} at which the new node shall be.
   * @param <N>      The Node type.
   * @return A node instance matching the type parameter wrapped in {@link CompletableFuture}.
   */
  <N extends Node> CompletableFuture<N> createAndLoadNode(NodeType<N> type, Location location);

  /**
   * Loads, modifies and saves a node with given {@link UUID} asynchronously.
   *
   * @param id      The {@link UUID} of the node to edit.
   * @param updater A consumer that will be applied to the requested node once loaded.
   * @return A {@link CompletableFuture} indicating the completion of the process.
   */
  CompletableFuture<Void> modifyNode(UUID id, Consumer<Node> updater);

  <N extends Node> CompletableFuture<Optional<N>> loadNode(UUID id);

  <N extends Node> CompletableFuture<N> insertGlobalGroupAndSave(N node);

  <N extends Node> CompletableFuture<Optional<N>> loadNode(NodeType<N> type, UUID id);

  CompletableFuture<Collection<Node>> loadNodes();

  CompletableFuture<Collection<Node>> loadNodes(Collection<UUID> ids);

  <M extends Modifier> CompletableFuture<Map<Node, Collection<M>>> loadNodes(NamespacedKey modifier);

  CompletableFuture<Void> saveNode(Node node);

  /**
   * Deletes a collection of nodes from storage asynchronously.
   * A call of this method must fire the according {@link NodeDeleteEvent}.
   * After successfull completion, all given {@link Node}s, all according {@link Edge}s,
   * {@link NodeGroup}- and {@link NodeType} mappings must be deleted.
   *
   * @param nodes A collection of nodes to delete.
   * @return A {@link CompletableFuture} indicating the completion of the process.
   */
  CompletableFuture<Void> deleteNodes(Collection<UUID> nodes);

  CompletableFuture<Map<UUID, Collection<Edge>>> loadEdgesTo(Collection<UUID> nodes);

  // Groups
  CompletableFuture<NodeGroup> createAndLoadGroup(NamespacedKey key);

  CompletableFuture<Optional<NodeGroup>> loadGroup(NamespacedKey key);

  CompletableFuture<Map<UUID, Collection<NodeGroup>>> loadGroups(Collection<UUID> ids);

  CompletableFuture<Map<Node, Collection<NodeGroup>>> loadGroupsOfNodes(Collection<Node> ids);

  CompletableFuture<Collection<NodeGroup>> loadGroups(Range range);

  CompletableFuture<Collection<NodeGroup>> loadGroups(UUID node);

  CompletableFuture<Collection<NodeGroup>> loadGroupsByMod(Collection<NamespacedKey> keys);

  <M extends Modifier> CompletableFuture<Collection<NodeGroup>> loadGroups(NamespacedKey modifier);

  CompletableFuture<Collection<NodeGroup>> loadAllGroups();

  CompletableFuture<Void> saveGroup(NodeGroup group);

  CompletableFuture<Void> deleteGroup(NodeGroup group);

  CompletableFuture<Void> modifyGroup(NamespacedKey key, Consumer<NodeGroup> update);

  // Find Data
  CompletableFuture<DiscoverInfo> createAndLoadDiscoverinfo(UUID player, NamespacedKey key,
                                                            LocalDateTime time);

  CompletableFuture<Optional<DiscoverInfo>> loadDiscoverInfo(UUID player, NamespacedKey key);

  CompletableFuture<Void> deleteDiscoverInfo(DiscoverInfo info);

  // Visualizer

  <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerType<VisualizerT>>> loadVisualizerType(
      NamespacedKey key);

  CompletableFuture<Map<NamespacedKey, VisualizerType<?>>> loadVisualizerTypes(
      Collection<NamespacedKey> keys);

  <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Void> saveVisualizerType(
      NamespacedKey key, VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<VisualizerT> createAndLoadVisualizer(
      VisualizerType<VisualizerT> type, NamespacedKey key);

  CompletableFuture<Collection<PathVisualizer<?, ?>>> loadVisualizers();

  <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Collection<VisualizerT>> loadVisualizers(
      VisualizerType<VisualizerT> type);

  <VisualizerT extends PathVisualizer<?, ?>> CompletableFuture<Optional<VisualizerT>> loadVisualizer(
      NamespacedKey key);

  CompletableFuture<Void> saveVisualizer(PathVisualizer<?, ?> visualizer);

  CompletableFuture<Void> deleteVisualizer(PathVisualizer<?, ?> visualizer);
}
