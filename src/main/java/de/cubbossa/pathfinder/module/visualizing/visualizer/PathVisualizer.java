package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PermissionHolder;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;

public interface PathVisualizer<T extends PathVisualizer<T, D>, D>
    extends Keyed, Named, PermissionHolder {

  VisualizerType<T> getType();

  D prepare(List<Node<?>> nodes, Player player);

  void play(VisualizerContext<D> context);

  default void destruct(Player player, D data) {

  }

  int getInterval();

  void setInterval(int interval);

  interface Property<V extends PathVisualizer<?, ?>, T> {

    String getKey();

    Class<T> getType();

    void setValue(V visualizer, T value);

    T getValue(V visualizer);

    boolean isVisible();

    @Getter
    @RequiredArgsConstructor
    class SimpleProperty<V extends PathVisualizer<?, ?>, T> implements Property<V, T> {
      private final String key;
      private final Class<T> type;
      private final boolean visible;
      private final Function<V, T> getter;
      private final BiConsumer<V, T> setter;

      @Override
      public void setValue(V visualizer, T value) {
        setter.accept(visualizer, value);
      }

      @Override
      public T getValue(V visualizer) {
        return getter.apply(visualizer);
      }
    }
  }

  record VisualizerContext<D>(List<Player> players, int interval, long time, D data) {

    public Player player() {
      return players.get(0);
    }

  }
}
