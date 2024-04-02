package de.cubbossa.pathfinder.visualizer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.storage.VisualizerStorageImplementation;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.InternalVisualizerStorageImplementationImpl;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * VisualizerTypes contain multiple methods to manage visualizers with common properties.
 * This includes the edit command, serialization and deserialization.
 *
 * @param <T> The class of the Visualizer. This can also be a common parent class or interface.
 */
@Getter
@Setter
public abstract class AbstractVisualizerType<T extends PathVisualizer<?, ?>>
    implements VisualizerType<T> {

  /**
   * The NamespacedKey of this visualizer that is used as type identifier when loading data.
   */
  private final NamespacedKey key;
  private VisualizerStorageImplementation<T> storage = null;

  public AbstractVisualizerType(NamespacedKey key) {
    this.key = key;
  }

  @Override
  public VisualizerStorageImplementation<T> getStorage() {
    // TODO cache pls
    PathFinder pathFinder = PathFinderProvider.get();
    if (pathFinder == null || pathFinder.getStorage() == null) {
      return null;
    }
    if (PathFinderProvider.get().getStorage().getImplementation() instanceof InternalVisualizerStorageImplementation visStorage) {
      setStorage(new InternalVisualizerStorageImplementationImpl<>(visStorage));
    }
    return storage;
  }

  @Override
  public String getCommandName() {
    return key.getKey();
  }

  @Override
  public void deserialize(final T visualizer, Map<String, Object> values) {
  }

  @Override
  public Map<String, Object> serialize(T visualizer) {
    return new LinkedHashMap<>();
  }

  public <V extends PathVisualizer<?, ?>, T2> void setProperty(PathPlayer<?> sender, V visualizer,
                                                               AbstractVisualizer.Property<V, T2> prop,
                                                               T2 val) {
    setProperty(sender, visualizer, val, prop.getKey(),
        () -> prop.getValue(visualizer), v -> prop.setValue(visualizer, v));
  }

  public <T2> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T2 value,
                               String property, Supplier<T2> getter,
                               Consumer<T2> setter) {
    setProperty(sender, visualizer, value, property, getter, setter, t ->
        Component.text(t == null ? "null" : t.toString()));
  }

  public <T2> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T2 value,
                               String property, Supplier<T2> getter,
                               Consumer<T2> setter, Function<T2, ComponentLike> formatter) {
    setProperty(sender, visualizer, value, property, getter, setter,
        (s, t) -> Placeholder.component(s, formatter.apply(t)));
  }

  public <T2> void setProperty(PathPlayer<?> sender, PathVisualizer<?, ?> visualizer, T2 value,
                               String property, Supplier<T2> getter,
                               Consumer<T2> setter, BiFunction<String, T2, TagResolver> formatter) {
    T2 old = getter.get();
    if (!PathFinderProvider.get().getEventDispatcher().dispatchVisualizerChangeEvent(visualizer)) {
      sender.sendMessage(Messages.CMD_VIS_SET_PROP_ERROR.formatted(
          Messages.formatter().namespacedKey("key", visualizer.getKey()),
          Placeholder.parsed("property", property)
      ));
      return;
    }
    setter.accept(value);
    PathFinderProvider.get().getStorage()
        .saveVisualizer(visualizer)
        .thenCompose(unused -> PathFinderProvider.get().getStorage().loadVisualizerType(visualizer.getKey()).thenAccept(optType -> {
          sender.sendMessage(Messages.CMD_VIS_SET_PROP.formatted(
              Messages.formatter().namespacedKey("key", visualizer.getKey()),
              Placeholder.component("type", Component.text(
                  optType.map(VisualizerType::getCommandName).orElse("unknown"))),
              Placeholder.parsed("property", property),
              formatter.apply("old-value", old),
              formatter.apply("value", value)
          ));
        }))
        .exceptionally(throwable -> {
          sender.sendMessage(Messages.CMD_VIS_SET_PROP_ERROR.formatted(
              Messages.formatter().namespacedKey("key", visualizer.getKey()),
              Placeholder.parsed("property", property)
          ));
          throwable.printStackTrace();
          return null;
        });
  }

  protected <A, V extends PathVisualizer<?, ?>> Argument<?> subCommand(String node,
                                                                       Argument<A> argument,
                                                                       AbstractVisualizer.Property<V, A> property) {
    return new LiteralArgument(node).then(argument.executes((commandSender, args) -> {
      if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
        setProperty(AbstractPathFinder.getInstance().wrap(commandSender), (V) visualizer, property, args.getUnchecked(1));
      } else {
        throw new WrapperCommandSyntaxException(new CommandSyntaxException(
            CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(),
            () -> "Wrong usage. First argument has to be a path visualizer."
        ));
      }
    }));
  }

  protected <A, V extends PathVisualizer<?, ?>> void loadProperty(Map<String, Object> values,
                                                                  V visualizer,
                                                                  AbstractVisualizer.Property<V, A> property) {
    loadProperty(values, property.getKey(), property.getType(),
        a -> property.setValue(visualizer, a));
  }

  protected <A> void loadProperty(Map<String, Object> values, String key, Class<A> expectedType,
                                  Consumer<A> loader) {
    if (!values.containsKey(key)) {
      // can be ignored, unset data will be replaced with defaults
      return;
    }
    Object value = values.get(key);
    if (!value.getClass().equals(expectedType)) {
      throw new DataStorageException(
          "Data for field '" + key + "' of visualizer is not of expected type: "
              + expectedType.getSimpleName() + ". Instead: " + value.getClass().getSimpleName());
    }
    loader.accept((A) value);
  }

  protected <A extends Enum<A>> void loadEnumProperty(Map<String, Object> values, String key,
                                                      Class<A> expectedType, Consumer<A> loader) {
    loadProperty(values, key, String.class,
        s -> loader.accept(Enum.valueOf(expectedType, s.toUpperCase())));
  }

  protected <V extends PathVisualizer<?, ?>> void serialize(Map<String, Object> map,
                                                            AbstractVisualizer.Property<V, ?> property,
                                                            V visualizer) {
    map.put(property.getKey(), property.getValue(visualizer));
  }
}
