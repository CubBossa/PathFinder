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
    logger.log(Level.INFO, "setLogger(Logger logger)");
    implementation.setLogger(logger);
  }

  @Override
  public void setWorldLoader(WorldLoader worldLoader) {
    logger.log(Level.INFO, "setWorldLoader(WorldLoader worldLoader)");
    implementation.setWorldLoader(worldLoader);
  }

  @Override
  public void saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping) {
    logger.log(Level.INFO, "saveNodeTypeMapping(Map<UUID, NodeType<?>> typeMapping)");
    implementation.saveNodeTypeMapping(typeMapping);
  }

  @Override
  public Map<UUID, NodeType<?>> loadNodeTypeMapping(Collection<UUID> nodes) {
    logger.log(Level.INFO, "loadNodeTypeMapping(Collection<UUID> nodes)");
    return implementation.loadNodeTypeMapping(nodes);
  }

  @Override
  public void deleteNodeTypeMapping(Collection<UUID> nodes) {
    logger.log(Level.INFO, "deleteNodeTypeMapping(Collection<UUID> nodes)");
    implementation.deleteNodeTypeMapping(nodes);
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesFrom(Collection<UUID> start) {
    logger.log(Level.INFO, "loadEdgesFrom(Collection<UUID> start)");
    return implementation.loadEdgesFrom(start);
  }

  @Override
  public Map<UUID, Collection<Edge>> loadEdgesTo(Collection<UUID> end) {
    logger.log(Level.INFO, "loadEdgesTo(Collection<UUID> end)");
    return implementation.loadEdgesTo(end);
  }

  @Override
  public void deleteEdgesTo(Collection<UUID> end) {
    logger.log(Level.INFO, "deleteEdgesTo(Collection<UUID> end)");
    implementation.deleteEdgesTo(end);
  }

  @Override
  public NodeGroup createAndLoadGroup(NamespacedKey key) {
    logger.log(Level.INFO, "createAndLoadGroup(NamespacedKey key)");
    return implementation.createAndLoadGroup(key);
  }

  @Override
  public Collection<NodeGroup> loadGroupsByMod(Collection<NamespacedKey> key) {
    logger.log(Level.INFO, "loadGroupsByMod(Collection<NamespacedKey> key)");
    return implementation.loadGroupsByMod(key);
  }

  @Override
  public Map<UUID, Collection<NodeGroup>> loadGroups(Collection<UUID> ids) {
    logger.log(Level.INFO, "loadGroups(Collection<UUID> ids)");
    return implementation.loadGroups(ids);
  }

  @Override
  public List<NodeGroup> loadGroups(Range range) {
    logger.log(Level.INFO, "loadGroups(Range range)");
    return implementation.loadGroups(range);
  }

  @Override
  public Collection<NodeGroup> loadGroups(UUID node) {
    logger.log(Level.INFO, "loadGroups(UUID nodes)");
    return implementation.loadGroups(node);
  }

  @Override
  public <M extends Modifier> Collection<NodeGroup> loadGroups(NamespacedKey modifier) {
    logger.log(Level.INFO, "loadGroups(NamespacedKey modifier)");
    return implementation.loadGroups(modifier);
  }

  @Override
  public Collection<NodeGroup> loadAllGroups() {
    logger.log(Level.INFO, "loadAllGroups()");
    return implementation.loadAllGroups();
  }

  @Override
  public Collection<UUID> loadGroupNodes(NodeGroup group) {
    logger.log(Level.INFO, "loadGroupNodes(NodeGroup group)");
    return implementation.loadGroupNodes(group);
  }

  @Override
  public void saveGroup(NodeGroup group) {
    logger.log(Level.INFO, "saveGroup(NodeGroup group)");
    implementation.saveGroup(group);
  }

  @Override
  public void deleteGroup(NodeGroup group) {
    logger.log(Level.INFO, "deleteGroup(NodeGroup group)");
    implementation.deleteGroup(group);
  }

  @Override
  public DiscoverInfo createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time) {
    logger.log(Level.INFO, "createAndLoadDiscoverinfo(UUID player, NamespacedKey key, LocalDateTime time)");
    return implementation.createAndLoadDiscoverinfo(player, key, time);
  }

  @Override
  public Optional<DiscoverInfo> loadDiscoverInfo(UUID player, NamespacedKey key) {
    logger.log(Level.INFO, "loadDiscoverInfo(UUID player, NamespacedKey key)");
    return implementation.loadDiscoverInfo(player, key);
  }

  @Override
  public void deleteDiscoverInfo(DiscoverInfo info) {
    logger.log(Level.INFO, "deleteDiscoverInfo(DiscoverInfo info)");
    implementation.deleteDiscoverInfo(info);
  }

  @Override
  public void saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types) {
    logger.log(Level.INFO, "saveVisualizerTypeMapping(Map<NamespacedKey, VisualizerType<?>> types)");
    implementation.saveVisualizerTypeMapping(types);
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> loadVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    logger.log(Level.INFO, "loadVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    return implementation.loadVisualizerTypeMapping(keys);
  }

  @Override
  public void deleteVisualizerTypeMapping(Collection<NamespacedKey> keys) {
    logger.log(Level.INFO, "deleteVisualizerTypeMapping(Collection<NamespacedKey> keys)");
    implementation.deleteVisualizerTypeMapping(keys);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    logger.log(Level.INFO, "createAndLoadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    return implementation instanceof InternalVisualizerDataStorage vs ?
        vs.createAndLoadInternalVisualizer(type, key) : null;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    logger.log(Level.INFO, "loadInternalVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key)");
    return implementation instanceof InternalVisualizerDataStorage vs ?
        vs.loadInternalVisualizer(type, key) : Optional.empty();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadInternalVisualizers(VisualizerType<VisualizerT> type) {
    logger.log(Level.INFO, "loadInternalVisualizers(VisualizerType<VisualizerT> type)");
    return implementation instanceof InternalVisualizerDataStorage vs ?
        vs.loadInternalVisualizers(type) : new HashMap<>();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer) {
    logger.log(Level.INFO, "saveInternalVisualizer(VisualizerType<VisualizerT> type, VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerDataStorage vs) {
      vs.saveInternalVisualizer(type, visualizer);
    }
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteInternalVisualizer(VisualizerT visualizer) {
    logger.log(Level.INFO, "deleteInternalVisualizer(VisualizerT visualizer)");
    if (implementation instanceof InternalVisualizerDataStorage vs) {
      vs.deleteInternalVisualizer(visualizer);
    }
  }

  @Override
  public Waypoint createAndLoadWaypoint(Location location) {
    logger.log(Level.INFO, "createAndLoadWaypoint(Location location)");
    return implementation instanceof WaypointDataStorage ws ? ws.createAndLoadWaypoint(location) : null;
  }

  @Override
  public Optional<Waypoint> loadWaypoint(UUID uuid) {
    logger.log(Level.INFO, "loadWaypoint(UUID uuid)");
    return implementation instanceof WaypointDataStorage ws ? ws.loadWaypoint(uuid) : Optional.empty();
  }

  @Override
  public Collection<Waypoint> loadWaypoints(Collection<UUID> ids) {
    logger.log(Level.INFO, "loadWaypoints(Collection<UUID> ids " + ids.size() + ")");
    return implementation instanceof WaypointDataStorage ws ? ws.loadWaypoints(ids) : new HashSet<>();
  }

  @Override
  public Collection<Waypoint> loadAllWaypoints() {
    logger.log(Level.INFO, "loadAllWaypoints()");
    return implementation instanceof WaypointDataStorage ws ? ws.loadAllWaypoints() : new HashSet<>();
  }

  @Override
  public void saveWaypoint(Waypoint node) {
    logger.log(Level.INFO, "saveWaypoint(Waypoint nodes)");
    if (implementation instanceof WaypointDataStorage ws) {
      ws.saveWaypoint(node);
    }
  }

  @Override
  public void deleteWaypoints(Collection<Waypoint> waypoints) {
    logger.log(Level.INFO, "deleteWaypoints(Collection<Waypoint> waypoints)");
    if (implementation instanceof WaypointDataStorage ws) {
      ws.deleteWaypoints(waypoints);
    }
  }
}
