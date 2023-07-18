package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
public class VisualizerTypeRegistryImpl implements VisualizerTypeRegistry {

  @Getter
  private static VisualizerTypeRegistryImpl instance;

  @Getter
  @Setter
  private VisualizerType<?> defaultVisualizerType;
  private final HashedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> visualizerTypes;

  public VisualizerTypeRegistryImpl() {
    instance = this;

    this.visualizerTypes = new HashedRegistry<>();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getDefaultType() {
    return (VisualizerType<VisualizerT>) defaultVisualizerType;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void setDefaultType(VisualizerType<VisualizerT> type) {
    defaultVisualizerType = type;
  }

  @Override
  public @Nullable <T extends PathVisualizer<?, ?>> Optional<VisualizerType<T>> getType(
          NamespacedKey typeKey) {
    return Optional.ofNullable((VisualizerType<T>) visualizerTypes.get(typeKey));
  }

  @Override
  public <T extends PathVisualizer<?, ?>> void registerVisualizerType(VisualizerType<T> type) {
    visualizerTypes.put(type);
  }

  @Override
  public void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type) {
    visualizerTypes.remove(type.getKey());
  }

  @Override
  public KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getTypes() {
    return new HashedRegistry<>(visualizerTypes);
  }
}
