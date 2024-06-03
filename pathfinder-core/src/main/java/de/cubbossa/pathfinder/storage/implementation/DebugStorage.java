package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.Range;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.DiscoverInfo;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.WaypointStorageImplementation;
import de.cubbossa.pathfinder.storage.WorldLoader;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

public class DebugStorage implements StorageImplementation, WaypointStorageImplementation, InternalVisualizerStorageImplementation {

  private final StorageImplementation implementation;
  private final Logger logger;

  public DebugStorage(StorageImplementation implementation, Logger logger) {
    this.implementation = implementation;
    this.logger = logger;
  }

  @Override
  public @Nullable ExecutorService service(ThreadFactory factory) {
    return implementation.service(factory);
  }

  @Override
  public void init() throws Exception {
    implementation.init();
  }

  @Override
  public void shutdown() {
    implementation.shutdown();
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  private Map<Long, Long> threadStartMap = new HashMap<>();

  private void debug(String msg) {
    long id = Thread.currentThread().getId();
    Long dur = null;
    if (!threadStartMap.containsKey(id)) {
      threadStartMap.put(id, System.nanoTime());
    } else {
      dur = System.nanoTime() - threadStartMap.remove(id);
    }
    logger.log(Level.INFO, msg + "\u001B[90m" + "(" + Thread.currentThread().getName() + (dur == null ? "" : ", " + (dur / 1_000_000.) + "ms") + ")" + "\u001B[0m");
  }

  @Override
  public void setLogger(Logger logger) {
    debug("> setLogger(Logger logger)");
    implementation.logger = logger;
    debug("< setLogger(Logger logger)");
  }

  @Override
  public void setWorldLoader(WorldLoader worldLoader) {
    debug("> setWorldLoader(WorldLoader worldLoader)");
    implementation.setWorldLoader(worldLoader);
    debug("< setWorldLoader(WorldLoader worldLoader)");
  }

  @Override
  public void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping) {
    debug("> saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping)");
    implementation.saveNodeTypeMapping(typeMapping);
    debug("< saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping)");
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes) {
    debug("> loadNodeTypeMapping(Collection<UUID> nodes)");
    var x = implementation.loadNodeTypeMapping(nodes);
    debug("< loadNodeTypeMapping(Collection<UUID> nodes)");
    return x;
  }

  @Override
  public void deleteNodeTypeMapping(Collection<UUID> nodes) {
    debug("> deleteNodeTypeMapping(Collection<UUID> nodes)");
    implementation.deleteNodeTypeMapping(nodes);
    debug("< deleteNodeTypeMapping(Collection<UUID> nodes)");
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesFrom(Collection<UUID> start) {
    debug("> loadEdgesFrom(Collection<UUID> start)");
    var x = implementation.loadEdgesFrom(start);
    debug("< loadEdgesFrom(Collection<UUID> start)");
    return x;
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesTo(Collection<UUID> end) {
    debug("> loadEdgesTo(Collection<UUID> end)");
    var x = implementation.loadEdgesTo(end);
    debug("< loadEdgesTo(Collection<UUID> end)");
    return x;
  }

  @Override
  public void deleteEdgesTo(Collection<UUID> end) {
    debug("> deleteEdgesTo(Collection<UUID> end)");
    implementation.deleteEdgesTo(end);
    debug("< deleteEdgesTo(Collection<UUID> end)");
  }

  @Override
  public NodeGroup createAndLoadGroup(NamespacedKey key) {
    debug("> createAndLoadGroup(NamespacedKey key)");
    var x = implementation.createAndLoadGroup(key);
    debug("< createAndLoadGroup(NamespacedKey key)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key) {
    debug("> loadGroupsByMod(Collection<NamespacedKey> key)");
    var x = implementation.loadGroupsByMod(key);
    debug("< loadGroupsByMod(Collection<NamespacedKey> key)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadGroups(Collection<NamespacedKey> keys) {
    debug("> loadGroups(Collection<NamespacedKey> keys)");
    var x = implementation.loadGroups(keys);
    debug("< loadGroups(Collection<NamespacedKey> keys)");
    return x;
  }

  @Override
  public Map<UUID, Collection<NodeGroup>> loadGroupsByNodes(Collection<UUID> ids) {
    debug("> loadGroups(Collection<UUID> ids " + ids.size() + ")");
    var x = implementation.loadGroupsByNodes(ids);
    debug("< loadGroups(Collection<UUID> ids)");
    return x;
  }

  @Override
  public List<NodeGroup> loadGroups(Range range) {
    debug("> loadGroups(Range range)");
    var x = implementation.loadGroups(range);
    debug("< loadGroups(Range range)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadGroupsByNode(UUID node) {
    debug("> loadGroups(UUID nodes)");
    var x = implementation.loadGroupsByNode(node);
    debug("< loadGroups(UUID nodes)");
    return x;
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier) {
    debug("> loadGroups(NamespacedKey modifier)");
    var x = implementation.loadGroups(modifier);
    debug("< loadGroups(NamespacedKey modifier)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadAllGroups() {
    debug("> loadAllGroups()");
    var x = implementation.loadAllGroups();
    debug("< loadAllGroups()");
    return x;
  }

  @Override
  public Collection<UUID> loadGroupNodes(NodeGroup group) {
    debug("> loadGroupNodes(NodeGroup group)");
    var x = implementation.loadGroupNodes(group);
    debug("< loadGroupNodes(NodeGroup group)");
    return x;
  }

  @Override
  public void saveGroup(NodeGroup group) {
    debug("> saveGroup(NodeGroup group)");
    implementation.saveGroup(group);
    debug("< saveGroup(NodeGroup group)");
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    debug("> deleteGroup(NodeGroup group)");
    implementation.deleteGroup(group);
    debug("< deleteGroup(NodeGroup group)");
  }

  @Override
  public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time) {
    debug("> createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time)");
    var x = implementation.createAndLoadDiscoverinfo(player, key, time);
    debug("< createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time)");
    return x;
  }

  @Override
  public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
    debug("> loadDiscoverInfo(UUID player, NamespacedKey key)");
    var x = implementation.loadDiscoverInfo(player, key);
    debug("< loadDiscoverInfo(UUID player, NamespacedKey key)");
    return x;
  }

  @Override
  public void deleteDiscoverInfo(DiscoverInfo info) {
    debug("> deleteDiscoverInfo(DiscoverInfo info)");
    implementation.deleteDiscoverInfo(info);
    debug("< deleteDiscoverInfo(DiscoverInfo info)");
  }

  @Override
  public void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types) {
    debug("> saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types)");
    implementation.saveVisualizerTypeMapping(types);
    debug("< saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types)");
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    debug("> loadVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    var x = implementation.loadVisualizerTypeMapping(keys);
    debug("< loadVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    return x;
  }

  @Override
  public void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    debug("> deleteVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    implementation.deleteVisualizerTypeMapping(keys);
    debug("< deleteVisualizerTypeMapping(Collection<NamespacedKey> keys)");
  }

  @Override
  public <VisualizerT extends AbstractVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(AbstractVisualizerType<VisualizerT> type, NamespacedKey key) {
    debug("> loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    Optional<VisualizerT> x = implementation instanceof InternalVisualizerStorageImplementation vs ?
        vs.loadInternalVisualizer(type, key) : Optional.empty();
    debug("< loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    return x;
  }

  @Override
  public <VisualizerT extends AbstractVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(AbstractVisualizerType<VisualizerT> type) {
    debug("> loadInternalVisualizers(VisualizerType<VisualizerT> type)");
    Map<NamespacedKey, VisualizerT> x = implementation instanceof InternalVisualizerStorageImplementation vs ?
        vs.loadInternalVisualizers(type) : new HashMap<>();
    debug("< loadInternalVisualizers(VisualizerType<VisualizerT> type)");
    return x;
  }

  @Override
  public <VisualizerT extends AbstractVisualizer<?, ?>> void saveInternalVisualizer(AbstractVisualizerType<VisualizerT> type, VisualizerT visualizer) {
    debug("> saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerStorageImplementation vs) {
      debug("< saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer)");
      vs.saveInternalVisualizer(type, visualizer);
    }
  }

  @Override
  public <VisualizerT extends AbstractVisualizer<?, ?>> void deleteInternalVisualizer(AbstractVisualizerType<VisualizerT> type, VisualizerT visualizer) {
    debug("> deleteInternalVisualizer(VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerStorageImplementation vs) {
      debug("< deleteInternalVisualizer(VisualizerT visualizer)");
      vs.deleteInternalVisualizer(type, visualizer);
    }
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    debug("> loadWaypoint(UUID uuid)");
    Optional<Waypoint> x = implementation instanceof WaypointStorageImplementation ws ? ws.loadWaypoint(uuid) : Optional.empty();
    debug("< loadWaypoint(UUID uuid)");
    return x;
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> ids) {
    debug("> loadWaypoints(Collection<UUID> ids " + ids.size() + ")");
    Collection<Waypoint> x = implementation instanceof WaypointStorageImplementation ws ? ws.loadWaypoints(ids) : new HashSet<>();
    debug("< loadWaypoints(Collection<UUID> ids " + ids.size() + ")");
    return x;
  }

  @Override
  public Collection<Waypoint> loadAllWaypoints() {
    debug("> loadAllWaypoints()");
    Collection<Waypoint> x = implementation instanceof WaypointStorageImplementation ws ? ws.loadAllWaypoints() : new HashSet<>();
    debug("< loadAllWaypoints()");
    return x;
  }

  @Override
  public void saveWaypoint(Waypoint node) {
    if (implementation instanceof WaypointStorageImplementation ws) {
      debug("> saveWaypoint(Waypoint nodes)");
      ws.saveWaypoint(node);
      debug("< saveWaypoint(Waypoint nodes)");
    }
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    debug("> deleteWaypoints(Collection<Waypoint> waypoints)");
    if (implementation instanceof WaypointStorageImplementation ws) {
      debug("< deleteWaypoints(Collection<Waypoint> waypoints)");
      ws.deleteWaypoints(waypoints);
    }
  }
}
