package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodeCurveLengthChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeLocationChangedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapCreatedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetCurveLengthEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetNameEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapSetVisualizerEvent;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.StringUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class RoadMapHandler {

  public static NodeType<Waypoint> WAYPOINT_TYPE =
      new NodeType<>(new NamespacedKey(PathPlugin.getInstance(), "waypoint"),
          "<color:#ff0000>Waypoint</color>", new ItemStack(Material.MAP), context -> {
        Waypoint waypoint = new Waypoint(context.id(), context.roadMap(), context.persistent());
        waypoint.setLocation(context.location());
        return waypoint;
      });

  @Getter
  private static RoadMapHandler instance;

  @Getter
  private final HashedRegistry<RoadMap> roadMaps;

  private final HashedRegistry<RoadMap> roadMapsWithEditModeAccess = new HashedRegistry<>();
  @Getter
  private final HashedRegistry<RoadMapEditor> roadMapEditors;
  private int nodeIdCounter;

  public RoadMapHandler() {
    instance = this;
    roadMaps = new HashedRegistry<>();
    roadMapEditors = new HashedRegistry<>();
    NodeTypeHandler.getInstance().registerNodeType(WAYPOINT_TYPE);
  }

  public void loadRoadMaps() {
    var nodesGroupMapping = PathPlugin.getInstance().getDatabase().loadNodeGroupNodes();

    roadMaps.clear();
    roadMaps.putAll(PathPlugin.getInstance().getDatabase().loadRoadMaps());
    for (RoadMap map : roadMaps) {
      map.loadNodesAndEdges(nodesGroupMapping);
    }
    nodeIdCounter = roadMaps.values().stream().flatMap(roadMap -> roadMap.getNodes().stream())
        .mapToInt(Node::getNodeId)
        .max().orElse(0);
  }

  public int requestNodeId() {
    return ++nodeIdCounter;
  }

  public boolean isRoadMapKeyUnique(NamespacedKey key) {
    return !roadMaps.containsKey(key);
  }

  /**
   * Creates a roadmap and announces the creation. Note, that the created roadmap has no visualizer yet. You may want to
   * assign one after calling this method.
   *
   * @param plugin       An instance of the plugin that is responsible for this RoadMap.
   * @param key          A string that represents this roadmap and is unique in combination with the name of the responsible plugin.
   * @param persistent   If the roadmap should persist beyond a restart of the server or the PathFinder plugin. Set it to false if
   *                     you want to create this roadmap everytime your plugin enables, like for a mini-game. Set it to true if
   *                     this roadmap should be created once and persist until someone deletes it manually.
   * @param allowEditing If the roadmap can be edited via edit mode / commands. Persistent roadmaps are always editable.
   * @return The newly created roadmap.
   * @throws IllegalArgumentException If another roadmap with this key already exists.
   */
  public RoadMap createRoadMap(Plugin plugin, String key, boolean persistent, boolean allowEditing)
      throws IllegalArgumentException {

    NamespacedKey nKey = new NamespacedKey(plugin, key);
    if (!isRoadMapKeyUnique(nKey)) {
      throw new IllegalArgumentException("Another roadmap with this key already exists.");
    }

    RoadMap rm = new RoadMap(
        nKey,
        StringUtils.insertInRandomHexString(StringUtils.capizalize(key)),
        null,
        1,
        persistent
    );
    roadMaps.put(rm);
    if (allowEditing || persistent) {
      roadMapsWithEditModeAccess.put(rm);
    }
    Bukkit.getPluginManager().callEvent(new RoadMapCreatedEvent(rm));
    return rm;
  }

  public void deleteRoadMap(RoadMap roadMap) {

    roadMaps.remove(roadMap.getKey());
    Bukkit.getPluginManager().callEvent(new RoadMapDeletedEvent(roadMap));
  }

  // Getters

  public @Nullable
  RoadMap getRoadMap(NamespacedKey key) {
    return roadMaps.get(key);
  }

  public Stream<RoadMap> getRoadMapsStream() {
    return roadMaps.values().stream();
  }

  // Editing

  public RoadMapEditor getRoadMapEditor(NamespacedKey key) {
    RoadMapEditor editor = roadMapEditors.get(key);
    if (editor == null) {
      RoadMap roadMap = roadMaps.get(key);
      if (roadMap == null) {
        throw new IllegalArgumentException(
            "No roadmap exists with key '" + key + "'. Cannot create editor.");
      }
      editor = new RoadMapEditor(roadMap);
      roadMapEditors.put(editor);
    }
    return editor;
  }

  public void cancelAllEditModes() {
    roadMapEditors.values().forEach(RoadMapEditor::cancelEditModes);
  }

  public boolean isPlayerEditingRoadMap(Player player) {
    return roadMapEditors.values().stream()
        .anyMatch(roadMapEditor -> roadMapEditor.isEditing(player));
  }

  public @Nullable NamespacedKey getRoadMapEditedBy(Player player) {
    return roadMapEditors.values().stream()
        .filter(re -> re.isEditing(player))
        .map(RoadMapEditor::getKey)
        .findFirst()
        .orElse(null);
  }

  // Setters

  public boolean setRoadMapName(RoadMap roadMap, String nameFormat) {
    String old = roadMap.getNameFormat();
    RoadMapSetNameEvent event = new RoadMapSetNameEvent(roadMap, nameFormat);
    roadMap.setNameFormat(nameFormat);

    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      roadMap.setNameFormat(old);
      return false;
    }
    return true;
  }

  public boolean setRoadMapCurveLength(RoadMap roadMap, double value) {
    double old = roadMap.getDefaultCurveLength();
    RoadMapSetCurveLengthEvent event = new RoadMapSetCurveLengthEvent(roadMap, value);
    roadMap.setDefaultCurveLength(event.getValue());

    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      roadMap.setDefaultCurveLength(old);
      return false;
    }
    return true;
  }

  public boolean setRoadMapVisualizer(RoadMap roadMap, PathVisualizer<?, ?> visualizer) {
    PathVisualizer<?, ?> old = roadMap.getVisualizer();
    RoadMapSetVisualizerEvent event = new RoadMapSetVisualizerEvent(roadMap, visualizer);
    roadMap.setVisualizer(event.getVisualizer());

    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      roadMap.setVisualizer(old);
      return false;
    }
    return true;
  }

  /**
   * This method changes the position of the node and calls the corresponding event.
   * If the event is not cancelled, the change will be updated to the database.
   * Don't call this method asynchronous, events can only be called in the main thread.
   * <p>
   * TO only modify the position without event or database update, simply call {@link Node#setLocation(Location)}
   *
   * @param nodes    The nodes to change the position for.
   * @param location The position to set. No world attribute is required, the roadmap attribute is used. Use {@link Location#toVector()}
   *                 to set a location.
   * @return true if the position was successfully set, false if the event was cancelled
   */
  public boolean setNodeLocation(NodeSelection nodes, Location location) {

    NodeTeleportEvent event = new NodeTeleportEvent(nodes, location);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return false;
    }
    for (Node node : nodes) {
      node.setLocation(event.getNewPositionModified());
    }
    Bukkit.getPluginManager()
        .callEvent(new NodeLocationChangedEvent(nodes, event.getNewPositionModified()));
    return true;
  }

  public void setNodeCurveLength(NodeSelection nodes, Double length) {
    nodes.forEach(node -> node.setCurveLength(length));
    Bukkit.getPluginManager().callEvent(new NodeCurveLengthChangedEvent(nodes, length));
  }
}
