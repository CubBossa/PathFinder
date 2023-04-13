package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.core.nodegroup.NoImplNodeGroupEditor;
import de.cubbossa.pathfinder.api.group.NodeGroupEditor;
import de.cubbossa.pathfinder.api.group.NodeGroupEditorFactory;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.location.LocationWeightSolver;
import de.cubbossa.pathfinder.util.location.LocationWeightSolverPreset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class NodeHandler {

  public static final NamespacedKey GROUP_GLOBAL = NamespacedKey.fromString("pathfinder:global");

  @Getter
  private static NodeHandler instance;

  private final PathFinder pathFinder;

  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final HashedRegistry<NodeGroupEditor> editors;

  public NodeHandler(PathFinder pathFinder) {
    instance = this;
    this.pathFinder = pathFinder;

    editors = new HashedRegistry<>();

    ServiceLoader<NodeGroupEditorFactory> loader = ServiceLoader.load(NodeGroupEditorFactory.class,
        PathPlugin.getInstance().getClass().getClassLoader());
    NodeGroupEditorFactory factory = loader.findFirst().orElse(null);
    editModeFactory = Objects.requireNonNullElseGet(factory,
        () -> g -> new NoImplNodeGroupEditor(g.getKey()));
  }

  public CompletableFuture<Graph<Node<?>>> createGraph(@Nullable PlayerNode player) {
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {
      Map<UUID, Node<?>> map = new HashMap<>();
      nodes.forEach(node -> map.put(node.getNodeId(), node));

      Graph<Node<?>> graph = new Graph<>();
      nodes.forEach(graph::addNode);
      for (Node<?> node : nodes) {
        for (Edge e : node.getEdges()) {
          Node<?> end = map.get(e.getEnd());
          graph.connect(node, end,
              node.getLocation().distance(end.getLocation()) * e.getWeightModifier());
        }
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

  public @Nullable NamespacedKey getEdited(Player player) {
    return editors.values().stream()
        .filter(e -> e.isEditing(player))
        .map(NodeGroupEditor::getKey)
        .findFirst().orElse(null);
  }

  public void toggleNodeGroupEditor(Player player, NamespacedKey key) {
    getNodeGroupEditor(key).thenAccept(nodeGroupEditor -> {
      nodeGroupEditor.toggleEditMode(player.getUniqueId());
    });
  }

  public CompletableFuture<NodeGroupEditor> getNodeGroupEditor(NamespacedKey key) {
    CompletableFuture<NodeGroupEditor> future = new CompletableFuture<>();
    NodeGroupEditor editor = editors.get(key);
    if (editor == null) {
      pathFinder.getStorage().loadGroup(key).thenAccept(g -> {
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
          NodeGroupEditor e = editModeFactory.apply(
              g.orElseThrow(() -> new IllegalArgumentException("No group exists with key '" + key + "'. Cannot create editor."))
          );
          editors.put(e);
          future.complete(e);
        });
      });
    } else {
      future.complete(editor);
    }
    return future.exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  public void cancelAllEditModes() {
    editors.values().forEach(NodeGroupEditor::cancelEditModes);
  }
}
