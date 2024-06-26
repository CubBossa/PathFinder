package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractVisualizer<ViewT extends PathView<PlayerT>, PlayerT>
    implements PathVisualizer<ViewT, PlayerT> {

  private final NamespacedKey key;

  @Nullable
  private String permission = null;

  public AbstractVisualizer(NamespacedKey key) {
    this.key = key;
  }

  @Getter
  @Setter
  public abstract class AbstractView implements PathView<PlayerT> {

    private final UUID targetViewerUuid;
    private final UpdatingPath path;
    private final List<Node> pathCache;
    private PathPlayer<PlayerT> targetViewer;
    Collection<PathPlayer<PlayerT>> viewers;

    public AbstractView(PathPlayer<PlayerT> targetViewer, UpdatingPath path) {
      this.targetViewerUuid = targetViewer.getUniqueId();
      this.path = path;
      this.pathCache = new ArrayList<>(calculatePath());
      this.targetViewer = targetViewer;
      viewers = new HashSet<>();
    }

    public List<Node> getPath() {
      return pathCache;
    }

    public List<Node> calculatePath() {
      try {
        return path.getNodes();
      } catch (NoPathFoundException e) {
        return Collections.emptyList();
      }
    }

    @Override
    public void addViewer(PathPlayer<PlayerT> player) {
      viewers.add(player);
      if (targetViewer == null && player.getUniqueId().equals(targetViewerUuid)) {
        targetViewer = player;
      }
    }

    @Override
    public void removeViewer(PathPlayer<PlayerT> player) {
      viewers.remove(player);
      if (targetViewer != null && targetViewer == player) {
        targetViewer = null;
      }
    }

    @Override
    public void removeAllViewers() {
      targetViewer = null;
      viewers.clear();
    }

    @Override
    public Collection<PathPlayer<PlayerT>> getViewers() {
      return new HashSet<>(viewers);
    }

    @Override
    public void update() {
      pathCache.clear();
      pathCache.addAll(calculatePath());
    }

    @Override
    public void dispose() {
      removeAllViewers();
    }
  }

  public interface Property<VisualizerT extends PathVisualizer<?, ?>, TypeT> {

    String getKey();

    Class<TypeT> getType();

    void setValue(VisualizerT visualizer, TypeT value);

    TypeT getValue(VisualizerT visualizer);
  }

  @Getter
  @RequiredArgsConstructor
  public static class PropertyImpl<Value extends PathVisualizer<?, ?>, Type>
      implements Property<Value, Type> {
    private final String key;
    private final Class<Type> type;
    private final Function<Value, Type> getter;

    private final BiConsumer<Value, Type> setter;

    @Override
    public void setValue(Value visualizer, Type value) {
      setter.accept(visualizer, value);
    }

    @Override
    public Type getValue(Value visualizer) {
      return getter.apply(visualizer);
    }
  }
}
