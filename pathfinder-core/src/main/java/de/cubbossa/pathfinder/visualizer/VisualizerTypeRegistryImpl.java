package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.misc.KeyedRegistry;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public class VisualizerTypeRegistryImpl implements VisualizerTypeRegistry {

  @Getter
  private static VisualizerTypeRegistryImpl instance;

  public final ExtensionPoint<VisualizerType> EXTENSION_POINT = new ExtensionPoint<>(VisualizerType.class);

  @Getter
  @Setter
  private VisualizerType<?> defaultVisualizerType;
  private final HashedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> visualizerTypes;

  public VisualizerTypeRegistryImpl(PathFinder pathFinder) {
    instance = this;

    this.visualizerTypes = new HashedRegistry<>();
    pathFinder.getDisposer().register(pathFinder, this);

    EXTENSION_POINT.getExtensions().forEach(this::registerVisualizerType);

    if (!visualizerTypes.isEmpty()) {
      defaultVisualizerType = visualizerTypes.values().stream()
          .filter(visualizerType -> visualizerType.getKey().getKey().equals("particle"))
          .findFirst()
          .orElse(visualizerTypes.values().iterator().next());
    }
  }

  @Override
  public void dispose() {
    instance = null;
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
    PathFinderProvider.get().getDisposer().register(this, type);
  }

  @Override
  public void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?>> type) {
    PathFinderProvider.get().getDisposer().unregister(type);
    visualizerTypes.remove(type.getKey());
  }

  @Override
  public KeyedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> getTypes() {
    return new HashedRegistry<>(visualizerTypes);
  }
}
