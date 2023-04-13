package de.cubbossa.pathfinder.module.visualizing;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.api.storage.VisualizerDataStorage;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;

/**
 * VisualizerTypes contain multiple methods to manage visualizers with common properties.
 * This includes the edit command, serialization and deserialization.
 *
 * @param <T> The class of the Visualizer. This can also be a common parent class or interface.
 */
@Getter
@Setter
public abstract class VisualizerType<T extends PathVisualizer<T, ?>> implements de.cubbossa.pathfinder.api.visualizer.VisualizerType<T> {

  /**
   * The NamespacedKey of this visualizer that is used as type identifier when loading data.
   */
  private final NamespacedKey key;
  private VisualizerDataStorage<T> storage = null;

  public VisualizerType(NamespacedKey key) {
    this.key = key;
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
    return null;
  }

  protected <V extends PathVisualizer<?, ?>> void serialize(Map<String, Object> map,
                                                            AbstractVisualizer.Property<V, ?> property,
                                                            V visualizer) {
    map.put(property.getKey(), property.getValue(visualizer));
  }

  protected <A, V extends PathVisualizer<?, ?>> ArgumentTree subCommand(String node,
                                                                        Argument<A> argument,
                                                                        AbstractVisualizer.Property<V, A> property) {
    return new LiteralArgument(node).then(argument.executes((commandSender, objects) -> {
      if (objects[0] instanceof PathVisualizer<?, ?> visualizer) {
        VisualizerHandler.getInstance()
            .setProperty(commandSender, (V) visualizer, property, (A) objects[1]);
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
}
