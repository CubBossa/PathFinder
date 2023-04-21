package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.events.visualizer.VisualizerPropertyChangedEvent;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.visualizer.impl.*;
import de.cubbossa.translations.TranslationHandler;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

@Getter
public class VisualizerHandler implements VisualizerTypeRegistry {

  // TODO refactor!

  public static final VisualizerType<ParticleVisualizer> PARTICLE_VISUALIZER_TYPE =
      new ParticleVisualizerType(PathPlugin.pathfinder("particle"));
  public static final VisualizerType<CombinedVisualizer> COMBINED_VISUALIZER_TYPE =
      new CombinedVisualizerType(PathPlugin.pathfinder("combined"));
  public static final VisualizerType<CompassVisualizer> COMPASS_VISUALIZER_TYPE =
      new CompassVisualizerType(PathPlugin.pathfinder("compass"));

  @Getter
  private static VisualizerHandler instance;

  private final HashedRegistry<VisualizerType<? extends PathVisualizer<?,?, ?>>> visualizerTypes;

  public VisualizerHandler() {
    instance = this;

    this.visualizerTypes = new HashedRegistry<>();

    visualizerTypes.put(PARTICLE_VISUALIZER_TYPE);
    visualizerTypes.put(COMBINED_VISUALIZER_TYPE);
    visualizerTypes.put(COMPASS_VISUALIZER_TYPE);
  }

  @Override
  public @Nullable <T extends PathVisualizer<T, ?, ?>> AbstractVisualizerType<T> getVisualizerType(
      NamespacedKey key) {
    return (AbstractVisualizerType<T>) visualizerTypes.get(key);
  }

  @Override
  public <T extends PathVisualizer<T, ?, ?>> void registerVisualizerType(VisualizerType<T> type) {
    visualizerTypes.put(type);
  }

  @Override
  public void unregisterVisualizerType(VisualizerType<? extends PathVisualizer<?, ?, ?>> type) {
    visualizerTypes.remove(type.getKey());
  }

  public <V extends PathVisualizer<?, ?, ?>, T> void setProperty(CommandSender sender, V visualizer,
                                                              AbstractVisualizer.Property<V, T> prop,
                                                              T val) {
    setProperty(sender, visualizer, val, prop.getKey(), prop.isVisible(),
        () -> prop.getValue(visualizer), v -> prop.setValue(visualizer, v));
  }

  public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter) {
    setProperty(sender, visualizer, value, property, visual, getter, setter,
        t -> Component.text(t.toString()));
  }

  public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter, Function<T, ComponentLike> formatter) {
    setProperty(sender, visualizer, value, property, visual, getter, setter,
        (s, t) -> Placeholder.component(s, formatter.apply(t)));
  }

  public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?, ?> visualizer, T value,
                              String property, boolean visual, Supplier<T> getter,
                              Consumer<T> setter, BiFunction<String, T, TagResolver> formatter) {
    T old = getter.get();
    setter.accept(value);
    Bukkit.getPluginManager()
        .callEvent(new VisualizerPropertyChangedEvent<>(visualizer, property, visual, old, value));
    TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_PROP.format(
        TagResolver.resolver("key", Messages.formatKey(visualizer.getKey())),
        Placeholder.component("name", visualizer.getDisplayName()),
        Placeholder.component("type", Component.text(visualizer.getType().getCommandName())),
        Placeholder.parsed("property", property),
        formatter.apply("old-value", old),
        formatter.apply("value", value)
    ), sender);
  }
}