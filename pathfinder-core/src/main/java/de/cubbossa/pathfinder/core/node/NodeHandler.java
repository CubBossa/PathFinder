package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.roadmap.NoImplNodeGroupEditor;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditor;
import de.cubbossa.pathfinder.core.roadmap.NodeGroupEditorFactory;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.location.LocationWeightSolver;
import de.cubbossa.pathfinder.util.location.LocationWeightSolverPreset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class NodeHandler {

  public static final NodeType<Waypoint> WAYPOINT_TYPE = new WaypointType();
  public static final NamespacedKey GROUP_GLOBAL = NamespacedKey.fromString("pathfinder:global");

  @Getter
  private static NodeHandler instance;

  private final ApplicationLayer dataStorage;
  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final HashedRegistry<NodeGroupEditor> editors;

  @Getter
  private final HashedRegistry<NodeType<?>> types;

  public NodeHandler(ApplicationLayer dataStorage) {
    instance = this;
    this.dataStorage = dataStorage;
    this.types = new HashedRegistry<>();

    editors = new HashedRegistry<>();
    NodeHandler.getInstance().registerNodeType(NodeHandler.WAYPOINT_TYPE);

    ServiceLoader<NodeGroupEditorFactory> loader = ServiceLoader.load(NodeGroupEditorFactory.class,
        PathPlugin.getInstance().getClass().getClassLoader());
    NodeGroupEditorFactory factory = loader.findFirst().orElse(null);
    editModeFactory = Objects.requireNonNullElseGet(factory,
        () -> g -> new NoImplNodeGroupEditor(g.getKey()));
  }

  public <T extends Node<T>> NodeType<T> getNodeType(NamespacedKey key) {
    return (NodeType<T>) types.get(key);
  }

  public void registerNodeType(NodeType<?> type) {
    types.put(type);
  }

  public void unregisterNodeType(NodeType<?> type) {
    types.remove(type.getKey());
  }

  public void unregisterNodeType(NamespacedKey key) {
    types.remove(key);
  }

  public CompletableFuture<Graph<Node<?>>> createGraph(@Nullable PlayerNode player) {
    return createGraph(player, new ArrayList<>());
  }

  public CompletableFuture<Graph<Node<?>>> createGraph(@Nullable PlayerNode player,
                                                       Collection<NodeGroup> filter) {
    return dataStorage.getNodesByGroups(filter).thenApply(nodes -> {
      Graph<Node<?>> graph = new Graph<>();
      nodes.forEach(graph::addNode);
      for (Node<?> node : nodes) {
        node.getEdges()
            .forEach(e -> graph.connect(e.getStart(), e.getEnd(), e.getWeightedLength()));
      }

      if (player != null) {
        graph.addNode(player);
        LocationWeightSolver<Node<?>> solver =
            LocationWeightSolverPreset.fromConfig(PathPlugin.getInstance()
                .getConfiguration().navigation.nearestLocationSolver);
        Map<Node<?>, Double> weighted = solver.solve(player, graph);

        weighted.forEach((node, weight) -> graph.connect(player, node, weight));
      }
      return graph;
    });
  }

  // Editing

  public void toggleNodeGroupEditor(Player player, NamespacedKey key) {
    getNodeGroupEditor(key).thenAccept(nodeGroupEditor -> {
      nodeGroupEditor.toggleEditMode(player.getUniqueId());
    });
  }

  public CompletableFuture<NodeGroupEditor> getNodeGroupEditor(NamespacedKey key) {
    NodeGroupEditor editor = editors.get(key);
    if (editor == null) {
      return PathFinderAPI.getInstance().getNodeGroup(key).thenApply(g -> {
        if (g == null) {
          throw new IllegalArgumentException(
              "No group exists with key '" + key + "'. Cannot create editor.");
        }
        NodeGroupEditor e = editModeFactory.apply(g);
        editors.put(e);
        return e;
      });
    }
    return CompletableFuture.completedFuture(editor);
  }

  public void cancelAllEditModes() {
    editors.values().forEach(NodeGroupEditor::cancelEditModes);
  }
}
