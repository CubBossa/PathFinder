package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.Range;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link StorageImplementation} handles the actual serializing and deserializing of the given objects.
 * To access pathfinder data, use an instance of {@link StorageAdapter} instead, which also handles caching and
 * combines different loading methods (e.g. loading a node, its edges and its groups) into one.
 */
public interface StorageImplementation {

  default @Nullable ExecutorService service(ThreadFactory factory) {
    return null;
  }

  /**
   * Initializes this storage implementation. Will be called by {@link StorageAdapter#init()} and will create
   * necessary files or objects. It assures that the instance can be used without issues afterward.
   *
   * @throws Exception Might call an exception. Not specified due to different implementations.
   */
  void init() throws Exception;

  void shutdown();

  Logger getLogger();

  void setLogger(Logger logger);

  void setWorldLoader(WorldLoader worldLoader);

  // ################################
  // #   Node Types
  // ################################

  void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping);

  Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes);

  void deleteNodeTypeMapping(Collection<UUID> nodes);

  // ################################
  // #   Edges
  // ################################

  Map<UUID, Collection<Edge>> loadEdgesFrom(Collection<UUID> start);

  Map<UUID, Collection<Edge>> loadEdgesTo(Collection<UUID> end);

  void deleteEdgesTo(Collection<UUID> end);

  // ################################
  // #   Groups
  // ################################

  NodeGroup createAndLoadGroup(NamespacedKey key);

  Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key);

  default Optional<NodeGroup> loadGroup(NamespacedKey key) {
    return loadGroups(Set.of(key)).stream().findAny();
  }

  Collection<NodeGroup> loadGroups(Collection<NamespacedKey> keys);

  Map<UUID, Collection<NodeGroup>> loadGroupsByNodes(Collection<UUID> ids);

  Collection<NodeGroup> loadGroupsByNode(UUID node);

  List<NodeGroup> loadGroups(Range range);

  <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier);

  Collection<NodeGroup> loadAllGroups();

  Collection<UUID> loadGroupNodes(NodeGroup group);

  void saveGroup(NodeGroup group);

  void deleteGroup(NodeGroup group);

  // ################################
  // #   Find Data
  // ################################

  DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time);

  Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key);

  void deleteDiscoverInfo(DiscoverInfo info);

  // ################################
  // #   Visualizer Types
  // ################################

  void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types);

  Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> keys);

  void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys);
}
