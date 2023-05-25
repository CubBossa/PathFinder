package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@Setter
public abstract class AbstractVisualizer<DataT, PlayerT>
    implements PathVisualizer<DataT, PlayerT> {

  private final NamespacedKey key;

  @Nullable
  private String permission = null;
  private int interval = 1;

  public AbstractVisualizer(NamespacedKey key) {
    this.key = key;
  }

  public interface Property<VisualizerT extends PathVisualizer<?, ?>, TypeT> {

    String getKey();

    Class<TypeT> getType();

    void setValue(VisualizerT visualizer, TypeT value);

    TypeT getValue(VisualizerT visualizer);

    boolean isVisible();
  }

  @Getter
  @RequiredArgsConstructor
  public static class SimpleProperty<Value extends PathVisualizer<?, ?>, Type>
      implements Property<Value, Type> {
    private final String key;
    private final Class<Type> type;
    private final boolean visible;
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
