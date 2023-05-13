package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.NodeGroupEditor;
import de.cubbossa.pathapi.editor.NodeGroupEditorFactory;
import de.cubbossa.pathapi.misc.LocationWeightSolver;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.graph.Graph;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.nodegroup.NoImplNodeGroupEditor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NodeHandler {

  @Getter
  private static NodeHandler instance;

  private final PathFinder pathFinder;

  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final Map<NamespacedKey, NodeGroupEditor> editors;

  public NodeHandler(PathFinder pathFinder) {
    instance = this;
    this.pathFinder = pathFinder;

    editors = new HashMap<>();

    ServiceLoader<NodeGroupEditorFactory> loader = ServiceLoader.load(NodeGroupEditorFactory.class,
        PathFinderProvider.get().getClass().getClassLoader());
    NodeGroupEditorFactory factory = loader.findFirst().orElse(null);
    editModeFactory = Objects.requireNonNullElseGet(factory,
        () -> g -> new NoImplNodeGroupEditor(g.getKey()));
  }

  public CompletableFuture<Graph<Node>> createGraph(@Nullable PlayerNode player) {
    return pathFinder.getStorage().loadNodes().thenApply(nodes -> {
      Map<UUID, Node> map = new HashMap<>();
      nodes.forEach(node -> map.put(node.getNodeId(), node));

      Graph<Node> graph = new Graph<>();
      nodes.forEach(graph::addNode);
      for (Node node : nodes) {
        for (Edge e : node.getEdges()) {
          Node end = map.get(e.getEnd());
          graph.connect(node, end,
              node.getLocation().distance(end.getLocation()) * e.getWeight());
        }
      }

      if (player != null) {
        graph.addNode(player);
        LocationWeightSolver<Node> solver = (LocationWeightSolver<Node>) pathFinder.getLocationWeightSolverRegistry().get(
            PathFinderProvider.get().getConfiguration().getNavigation().getNearestLocationSolver().getAlgorithm()
        );
        Map<Node, Double> weighted = solver.solve(player, graph);

        weighted.forEach((node, weight) -> graph.connect(player, node, weight));
      }
      return graph;
    });
  }

  // Editing

  public @Nullable NamespacedKey getEdited(PathPlayer<?> player) {
    return editors.values().stream()
        .filter(e -> e.isEditing(player))
        .map(NodeGroupEditor::getGroupKey)
        .findFirst().orElse(null);
  }

  public <PlayerT> void toggleNodeGroupEditor(PathPlayer<PlayerT> player, NamespacedKey key) {
    getNodeGroupEditor(key).thenAccept(nodeGroupEditor -> {
      ((NodeGroupEditor<PlayerT>) nodeGroupEditor).toggleEditMode(player);
    });
  }

  public <PlayerT> CompletableFuture<NodeGroupEditor<PlayerT>> getNodeGroupEditor(NamespacedKey key) {
    CompletableFuture<NodeGroupEditor<PlayerT>> future = new CompletableFuture<>();
    NodeGroupEditor<PlayerT> editor = editors.get(key);
    if (editor == null) {
      pathFinder.getStorage().loadGroup(key).thenAccept(g -> {
        NodeGroupEditor e = editModeFactory.apply(
            g.orElseThrow(() -> new IllegalArgumentException(
                "No group exists with key '" + key + "'. Cannot create editor."))
        );
        editors.put(key, e);
        future.complete(e);
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
