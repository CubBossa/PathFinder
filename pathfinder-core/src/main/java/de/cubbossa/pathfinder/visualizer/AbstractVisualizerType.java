package de.cubbossa.pathfinder.visualizer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.VisualizerStorageImplementationWrapper;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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
import org.jetbrains.annotations.ApiStatus;

/**
 * VisualizerTypes contain multiple methods to manage visualizers with common properties.
 * This includes the edit command, serialization and deserialization.
 *
 * @param <VisualizerT> The class of the Visualizer. This can also be a common parent class or interface.
 */
@Getter
@Setter
public abstract class AbstractVisualizerType<VisualizerT extends AbstractVisualizer<?, ?>>
    implements VisualizerType<VisualizerT> {

  /**
   * The NamespacedKey of this visualizer that is used as type identifier when loading data.
   */
  private final NamespacedKey key;
  private VisualizerStorageImplementationWrapper<VisualizerT> storage = null;

  public AbstractVisualizerType(NamespacedKey key) {
    this.key = key;
  }

  public VisualizerStorageImplementationWrapper<VisualizerT> getStorage() {
    // TODO cache pls
    PathFinder pathFinder = PathFinder.get();
    if (pathFinder == null || pathFinder.getStorage() == null) {
      return null;
    }
    if (PathFinder.get().getStorage().getImplementation() instanceof InternalVisualizerStorageImplementation visStorage) {
      setStorage(new VisualizerStorageImplementationWrapper<>(this, visStorage));
    }
    return storage;
  }

  public void deserialize(VisualizerT visualizer, Map<String, Object> values) {

  }

  public Map<String, Object> serialize(VisualizerT visualizer) {
    return new LinkedHashMap<>();
  }

  @ApiStatus.Internal
  public abstract VisualizerT createVisualizerInstance(NamespacedKey key);

  public VisualizerT createVisualizer(NamespacedKey key) {
    VisualizerT vis = createVisualizerInstance(key);
    PathFinder.get().getDisposer().register(this, vis);
    return vis;
  }

  @Override
  public void saveVisualizer(VisualizerT visualizer) {
    getStorage().saveVisualizer(visualizer);
  }

  @Override
  public VisualizerT createAndSaveVisualizer(NamespacedKey key) {
    VisualizerT vis = createVisualizer(key);
    saveVisualizer(vis);
    return vis;
  }

  @Override
  public Map<NamespacedKey, VisualizerT> loadVisualizers() {
    return getStorage().loadVisualizers();
  }

  @Override
  public Optional<VisualizerT> loadVisualizer(NamespacedKey key) {
    return getStorage().loadVisualizer(key);
  }

  @Override
  public void deleteVisualizer(VisualizerT visualizer) {
    getStorage().deleteVisualizer(visualizer);
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
    if (!PathFinder.get().getEventDispatcher().dispatchVisualizerChangeEvent(visualizer)) {
      sender.sendMessage(Messages.CMD_VIS_SET_PROP_ERROR
          .insertObject("visualizer", visualizer)
          .insertObject("vis", visualizer)
          .insertString("property", property));
      return;
    }
    setter.accept(value);
    PathFinder.get().getStorage()
        .saveVisualizer(visualizer)
        .thenCompose(unused -> PathFinder.get().getStorage().loadVisualizerType(visualizer.getKey()).thenAccept(optType -> {
          sender.sendMessage(Messages.CMD_VIS_SET_PROP
              .insertObject("vis", visualizer)
              .insertObject("visualizer", visualizer)
              .insertString("property", property)
              .insertObject("old-value", old)
              .insertObject("value", value));
        }))
        .exceptionally(throwable -> {
          sender.sendMessage(Messages.CMD_VIS_SET_PROP_ERROR
              .insertObject("visualizer", visualizer)
              .insertObject("vis", visualizer)
              .insertString("property", property));
          throwable.printStackTrace();
          return null;
        });
  }

  protected <A, V extends PathVisualizer<?, ?>> Argument<?> subCommand(String node,
                                                                       Argument<A> argument,
                                                                       AbstractVisualizer.Property<V, A> property) {
    return new LiteralArgument(node).then(argument.executes((commandSender, args) -> {
      if (args.get(0) instanceof PathVisualizer<?, ?> visualizer) {
        setProperty(PathPlayer.wrap(commandSender), (V) visualizer, property, args.getUnchecked(1));
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
