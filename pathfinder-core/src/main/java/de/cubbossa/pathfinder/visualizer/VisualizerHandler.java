package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.events.visualizer.VisualizerPropertyChangedEvent;
import de.cubbossa.pathfinder.util.HashedRegistry;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public class VisualizerHandler implements VisualizerTypeRegistry {

  @Getter
  private static VisualizerHandler instance;

  private final HashedRegistry<VisualizerType<? extends PathVisualizer<?, ?>>> visualizerTypes;

  public VisualizerHandler() {
    instance = this;

    this.visualizerTypes = new HashedRegistry<>();
  }

  @Override
  public @Nullable <T extends PathVisualizer<?, ?>> Optional<VisualizerType<T>> getType(
      NamespacedKey key) {
    return Optional.ofNullable((VisualizerType<T>) visualizerTypes.get(key));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> getType(
      VisualizerT visualizer) {
    return Optional.ofNullable(
        (VisualizerType<VisualizerT>) visualizerTypes.get(visualizer.getKey()));
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

  public <V extends PathVisualizer<?, ?>, T> void setProperty(PathPlayer<?> sender, V visualizer,
                                                              AbstractVisualizer.Property<V, T> prop,
                                                              T val) {
    setProperty(sender, visualizer, val, prop.getKey(), prop.isVisible(),
        () -> prop.getValue(visualizer), v -> prop.setValue(visualizer, v));
  }

  public <T> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter) {
    setProperty(sender, visualizer, value, property, visual, getter, setter,
        t -> Component.text(t.toString()));
  }

  public <T> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter, Function<T, ComponentLike> formatter) {
    setProperty(sender, visualizer, value, property, visual, getter, setter,
        (s, t) -> Placeholder.component(s, formatter.apply(t)));
  }

  public <T> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter, BiFunction<String, T, TagResolver> formatter) {
    T old = getter.get();
    setter.accept(value);
    Bukkit.getPluginManager()
        .callEvent(new VisualizerPropertyChangedEvent<>(visualizer, property, visual, old, value));
    sender.sendMessage(Messages.CMD_VIS_SET_PROP.formatted(
        TagResolver.resolver("key", Messages.formatKey(visualizer.getKey())),
        Placeholder.component("name", visualizer.getDisplayName()),
        Placeholder.component("type", Component.text(
            PathFinderProvider.get().getStorage().loadVisualizerType(visualizer.getKey()).join()
                .orElseThrow()
                .getCommandName())),
        Placeholder.parsed("property", property),
        formatter.apply("old-value", old),
        formatter.apply("value", value)
    ));
  }
}
