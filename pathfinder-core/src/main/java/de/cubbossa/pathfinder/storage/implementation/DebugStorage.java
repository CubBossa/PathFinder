package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.storage.WorldLoader;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DebugStorage implements StorageImplementation, WaypointDataStorage, InternalVisualizerDataStorage {

  private final StorageImplementation implementation;
  private final Logger logger;

  public DebugStorage(StorageImplementation implementation, Logger logger) {
    this.implementation = implementation;
    this.logger = logger;
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

  @Override
  public void setLogger(Logger logger) {
    logger.log(Level.INFO, "> setLogger(Logger logger)");
    implementation.setLogger(logger);
    logger.log(Level.INFO, "< setLogger(Logger logger)");
  }

  @Override
  public void setWorldLoader(WorldLoader worldLoader) {
    logger.log(Level.INFO, "> setWorldLoader(WorldLoader worldLoader)");
    implementation.setWorldLoader(worldLoader);
    logger.log(Level.INFO, "< setWorldLoader(WorldLoader worldLoader)");
  }

  @Override
  public void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping) {
    logger.log(Level.INFO, "> saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping)");
    implementation.saveNodeTypeMapping(typeMapping);
    logger.log(Level.INFO, "< saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping)");
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes) {
    logger.log(Level.INFO, "> loadNodeTypeMapping(Collection<UUID> nodes)");
    var x = implementation.loadNodeTypeMapping(nodes);
    logger.log(Level.INFO, "< loadNodeTypeMapping(Collection<UUID> nodes)");
    return x;
  }

  @Override
  public void deleteNodeTypeMapping(Collection<UUID> nodes) {
    logger.log(Level.INFO, "> deleteNodeTypeMapping(Collection<UUID> nodes)");
    implementation.deleteNodeTypeMapping(nodes);
    logger.log(Level.INFO, "< deleteNodeTypeMapping(Collection<UUID> nodes)");
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesFrom(Collection<UUID> start) {
    logger.log(Level.INFO, "> loadEdgesFrom(Collection<UUID> start)");
    var x = implementation.loadEdgesFrom(start);
    logger.log(Level.INFO, "< loadEdgesFrom(Collection<UUID> start)");
    return x;
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesTo(Collection<UUID> end) {
    logger.log(Level.INFO, "> loadEdgesTo(Collection<UUID> end)");
    var x = implementation.loadEdgesTo(end);
    logger.log(Level.INFO, "< loadEdgesTo(Collection<UUID> end)");
    return x;
  }

  @Override
  public void deleteEdgesTo(Collection<UUID> end) {
    logger.log(Level.INFO, "> deleteEdgesTo(Collection<UUID> end)");
    implementation.deleteEdgesTo(end);
    logger.log(Level.INFO, "< deleteEdgesTo(Collection<UUID> end)");
  }

  @Override
  public NodeGroup createAndLoadGroup(NamespacedKey key) {
    logger.log(Level.INFO, "> createAndLoadGroup(NamespacedKey key)");
    var x = implementation.createAndLoadGroup(key);
    logger.log(Level.INFO, "< createAndLoadGroup(NamespacedKey key)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key) {
    logger.log(Level.INFO, "> loadGroupsByMod(Collection<NamespacedKey> key)");
    var x = implementation.loadGroupsByMod(key);
    logger.log(Level.INFO, "< loadGroupsByMod(Collection<NamespacedKey> key)");
    return x;
  }

  @Override
  public Map<UUID, Collection<NodeGroup>> loadGroups(Collection<UUID> ids) {
    logger.log(Level.INFO, "> loadGroups(Collection<UUID> ids)");
    var x = implementation.loadGroups(ids);
    logger.log(Level.INFO, "< loadGroups(Collection<UUID> ids)");
    return x;
  }

  @Override
  public List<NodeGroup> loadGroups(Range range) {
    logger.log(Level.INFO, "> loadGroups(Range range)");
    var x = implementation.loadGroups(range);
    logger.log(Level.INFO, "< loadGroups(Range range)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
    logger.log(Level.INFO, "> loadGroups(UUID nodes)");
    var x = implementation.loadGroups(node);
    logger.log(Level.INFO, "< loadGroups(UUID nodes)");
    return x;
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier) {
    logger.log(Level.INFO, "> loadGroups(NamespacedKey modifier)");
    var x = implementation.loadGroups(modifier);
    logger.log(Level.INFO, "< loadGroups(NamespacedKey modifier)");
    return x;
  }

  @Override
  public Collection<NodeGroup> loadAllGroups() {
    logger.log(Level.INFO, "> loadAllGroups()");
    var x = implementation.loadAllGroups();
    logger.log(Level.INFO, "< loadAllGroups()");
    return x;
  }

  @Override
  public Collection<UUID> loadGroupNodes(NodeGroup group) {
    logger.log(Level.INFO, "> loadGroupNodes(NodeGroup group)");
    var x = implementation.loadGroupNodes(group);
    logger.log(Level.INFO, "< loadGroupNodes(NodeGroup group)");
    return x;
  }

  @Override
  public void saveGroup(NodeGroup group) {
    logger.log(Level.INFO, "> saveGroup(NodeGroup group)");
    implementation.saveGroup(group);
    logger.log(Level.INFO, "< saveGroup(NodeGroup group)");
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    logger.log(Level.INFO, "> deleteGroup(NodeGroup group)");
    implementation.deleteGroup(group);
    logger.log(Level.INFO, "< deleteGroup(NodeGroup group)");
  }

  @Override
  public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time) {
    logger.log(Level.INFO, "> createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time)");
    var x = implementation.createAndLoadDiscoverinfo(player, key, time);
    logger.log(Level.INFO, "< createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time)");
    return x;
  }

  @Override
  public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
    logger.log(Level.INFO, "> loadDiscoverInfo(UUID player, NamespacedKey key)");
    var x = implementation.loadDiscoverInfo(player, key);
    logger.log(Level.INFO, "< loadDiscoverInfo(UUID player, NamespacedKey key)");
    return x;
  }

  @Override
  public void deleteDiscoverInfo(DiscoverInfo info) {
    logger.log(Level.INFO, "> deleteDiscoverInfo(DiscoverInfo info)");
    implementation.deleteDiscoverInfo(info);
    logger.log(Level.INFO, "< deleteDiscoverInfo(DiscoverInfo info)");
  }

  @Override
  public void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types) {
    logger.log(Level.INFO, "> saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types)");
    implementation.saveVisualizerTypeMapping(types);
    logger.log(Level.INFO, "< saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types)");
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    logger.log(Level.INFO, "> loadVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    var x = implementation.loadVisualizerTypeMapping(keys);
    logger.log(Level.INFO, "< loadVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    return x;
  }

  @Override
  public void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    logger.log(Level.INFO, "> deleteVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    implementation.deleteVisualizerTypeMapping(keys);
    logger.log(Level.INFO, "< deleteVisualizerTypeMapping(Collection<NamespacedKey> keys)");
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    logger.log(Level.INFO, "> createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    var x = implementation instanceof InternalVisualizerDataStorage vs ?
        vs.createAndLoadInternalVisualizer(type, key) : null;
    logger.log(Level.INFO, "< createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    return x;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    logger.log(Level.INFO, "> loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    Optional<VisualizerT> x = implementation instanceof InternalVisualizerDataStorage vs ?
        vs.loadInternalVisualizer(type, key) : Optional.empty();
    logger.log(Level.INFO, "< loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    return x;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(VisualizerType<VisualizerT> type) {
    logger.log(Level.INFO, "> loadInternalVisualizers(VisualizerType<VisualizerT> type)");
    Map<NamespacedKey, VisualizerT> x = implementation instanceof InternalVisualizerDataStorage vs ?
        vs.loadInternalVisualizers(type) : new HashMap<>();
    logger.log(Level.INFO, "< loadInternalVisualizers(VisualizerType<VisualizerT> type)");
    return x;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer) {
    logger.log(Level.INFO, "> saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerDataStorage vs) {
      logger.log(Level.INFO, "< saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer)");
      vs.saveInternalVisualizer(type, visualizer);
    }
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteInternalVisualizer(VisualizerT visualizer) {
    logger.log(Level.INFO, "> deleteInternalVisualizer(VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerDataStorage vs) {
      logger.log(Level.INFO, "< deleteInternalVisualizer(VisualizerT visualizer)");
      vs.deleteInternalVisualizer(visualizer);
    }
  }

  @Override
  public Waypoint createAndLoadWaypoint(Location location) {
    logger.log(Level.INFO, "> createAndLoadWaypoint(Location location)");
    var x = implementation instanceof WaypointDataStorage ws ? ws.createAndLoadWaypoint(location) : null;
    logger.log(Level.INFO, "< createAndLoadWaypoint(Location location)");
    return x;
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    logger.log(Level.INFO, "> loadWaypoint(UUID uuid)");
    Optional<Waypoint> x = implementation instanceof WaypointDataStorage ws ? ws.loadWaypoint(uuid) : Optional.empty();
    logger.log(Level.INFO, "< loadWaypoint(UUID uuid)");
    return x;
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> ids) {
    logger.log(Level.INFO, "> loadWaypoints(Collection<UUID> ids " + ids.size() + ")");
    Collection<Waypoint> x = implementation instanceof WaypointDataStorage ws ? ws.loadWaypoints(ids) : new HashSet<>();
    logger.log(Level.INFO, "< loadWaypoints(Collection<UUID> ids " + ids.size() + ")");
    return x;
  }

  @Override
  public Collection<Waypoint> loadAllWaypoints() {
    logger.log(Level.INFO, "> loadAllWaypoints()");
    Collection<Waypoint> x = implementation instanceof WaypointDataStorage ws ? ws.loadAllWaypoints() : new HashSet<>();
    logger.log(Level.INFO, "< loadAllWaypoints()");
    return x;
  }

  @Override
  public void saveWaypoint(Waypoint node) {
    logger.log(Level.INFO, "> saveWaypoint(Waypoint nodes)");
    if (implementation instanceof WaypointDataStorage ws) {
      logger.log(Level.INFO, "< saveWaypoint(Waypoint nodes)");
      ws.saveWaypoint(node);
    }
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    logger.log(Level.INFO, "> deleteWaypoints(Collection<Waypoint> waypoints)");
    if (implementation instanceof WaypointDataStorage ws) {
      logger.log(Level.INFO, "< deleteWaypoints(Collection<Waypoint> waypoints)");
      ws.deleteWaypoints(waypoints);
    }
  }
}
