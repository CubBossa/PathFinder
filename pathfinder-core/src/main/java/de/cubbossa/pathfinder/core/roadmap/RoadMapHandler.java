package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.ServiceLoader;

public class RoadMapHandler {

  @Getter
  private static RoadMapHandler instance;

  @Getter
  private final HashedRegistry<RoadMap> roadMaps;

  private final HashedRegistry<RoadMap> roadMapsWithEditModeAccess = new HashedRegistry<>();

  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final HashedRegistry<NodeGroupEditor> roadMapEditors;
  private int nodeIdCounter;

  public RoadMapHandler() {
    instance = this;
    roadMaps = new HashedRegistry<>();
    roadMapEditors = new HashedRegistry<>();

    NodeHandler.getInstance().registerNodeType(NodeHandler.WAYPOINT_TYPE);

    ServiceLoader<NodeGroupEditorFactory> loader = ServiceLoader.load(NodeGroupEditorFactory.class,
        PathPlugin.getInstance().getClass().getClassLoader());
    NodeGroupEditorFactory factory = loader.findFirst().orElse(null);
    editModeFactory = Objects.requireNonNullElseGet(factory,
        () -> rm -> new NoImplNodeGroupEditor(rm.getKey()));
  }

  public void loadRoadMaps() {
    nodeIdCounter = NodeHandler.getInstance().getNodes().stream()
        .mapToInt(Node::getNodeId)
        .max().orElse(0);
  }

  public int requestNodeId() {
    return ++nodeIdCounter;
  }

  // Editing

  public NodeGroupEditor getRoadMapEditor(NamespacedKey key) {
    NodeGroupEditor editor = roadMapEditors.get(key);
    if (editor == null) {
      RoadMap roadMap = roadMaps.get(key);
      if (roadMap == null) {
        throw new IllegalArgumentException(
            "No roadmap exists with key '" + key + "'. Cannot create editor.");
      }
      editor = editModeFactory.apply(roadMap);
      roadMapEditors.put(editor);
    }
    return editor;
  }

  public void cancelAllEditModes() {
    roadMapEditors.values().forEach(NodeGroupEditor::cancelEditModes);
  }

  public boolean isPlayerEditingRoadMap(Player player) {
    return roadMapEditors.values().stream()
        .anyMatch(roadMapEditor -> roadMapEditor.isEditing(player));
  }

  public @Nullable NamespacedKey getRoadMapEditedBy(Player player) {
    return roadMapEditors.values().stream()
        .filter(re -> re.isEditing(player))
        .map(NodeGroupEditor::getKey)
        .findFirst()
        .orElse(null);
}}
