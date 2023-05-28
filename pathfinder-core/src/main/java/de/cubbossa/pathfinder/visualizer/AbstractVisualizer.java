package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.visualizer.PathView;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    private PathPlayer<PlayerT> targetViewer;
    Collection<PathPlayer<PlayerT>> viewers;

    public AbstractView() {
      targetViewer = null;
      viewers = new HashSet<>();
    }

    @Override
    public void addViewer(PathPlayer<PlayerT> player) {
      viewers.add(player);
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
  }

  public interface Property<VisualizerT extends PathVisualizer<?, ?>, TypeT> {

    String getKey();

    Class<TypeT> getType();

    void setValue(VisualizerT visualizer, TypeT value);

    TypeT getValue(VisualizerT visualizer);
  }

  @Getter
  @RequiredArgsConstructor
  public static class SimpleProperty<Value extends PathVisualizer<?, ?>, Type>
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
