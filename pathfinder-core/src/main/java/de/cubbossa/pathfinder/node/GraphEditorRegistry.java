package de.cubbossa.pathfinder.node;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.editor.GraphEditor;
import de.cubbossa.pathapi.editor.NodeGroupEditorFactory;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.nodegroup.NoImplGraphEditor;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.Getter;

public class GraphEditorRegistry implements Disposable {

  public static final ExtensionPoint<NodeGroupEditorFactory> EXTENSION_POINT = new ExtensionPoint<>(NodeGroupEditorFactory.class);

  @Getter
  private static GraphEditorRegistry instance;

  private final PathFinder pathFinder;

  private final NodeGroupEditorFactory editModeFactory;
  @Getter
  private final Map<NamespacedKey, GraphEditor> editors;

  public GraphEditorRegistry(PathFinder pathFinder) {
    instance = this;
    this.pathFinder = pathFinder;

    editors = new HashMap<>();

    if (EXTENSION_POINT.getExtensions().isEmpty()) {
      editModeFactory = g -> new NoImplGraphEditor(g.getKey());
    } else {
      editModeFactory = EXTENSION_POINT.getExtensions().iterator().next();
    }
  }

  // Editing

  public @Nullable NamespacedKey getEdited(PathPlayer<?> player) {
    return editors.values().stream()
        .filter(e -> e.isEditing(player))
        .map(GraphEditor::getGroupKey)
        .findFirst().orElse(null);
  }

  public <PlayerT> void toggleNodeGroupEditor(PathPlayer<PlayerT> player, NamespacedKey key) {
    getNodeGroupEditor(key).thenAccept(nodeGroupEditor -> {
      ((GraphEditor<PlayerT>) nodeGroupEditor).toggleEditMode(player);
    });
  }

  public <PlayerT> CompletableFuture<GraphEditor<PlayerT>> getNodeGroupEditor(NamespacedKey key) {
    CompletableFuture<GraphEditor<PlayerT>> future = new CompletableFuture<>();
    GraphEditor<PlayerT> editor = (GraphEditor<PlayerT>) editors.get(key);
    if (editor == null) {
      pathFinder.getStorage().loadGroup(key).thenAccept(g -> {
        GraphEditor e = editModeFactory.apply(
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
    editors.values().forEach(GraphEditor::cancelEditModes);
  }

  @Override
  public void dispose() {
    editors.forEach((key, nodeGroupEditor) -> {
      nodeGroupEditor.cancelEditModes();
      nodeGroupEditor.dispose();
    });
    instance = null;
  }
}
