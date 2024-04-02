package de.cubbossa.pathfinder.module.papi;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

@Getter
@Setter
@AutoService(VisualizerType.class)
public class PlaceholderVisualizerType
    extends AbstractVisualizerType<PlaceholderVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<PlaceholderVisualizer> {

  public PlaceholderVisualizerType() {
    super(AbstractPathFinder.pathfinder("placeholder"));
  }

  @Override
  public PlaceholderVisualizer create(NamespacedKey key) {
    return new PlaceholderVisualizer(key);
  }

  @Override
  public Message getInfoMessage(PlaceholderVisualizer element) {
    return Messages.CMD_VIS_PAPI_INFO.formatted(
        Placeholder.parsed("format-north", element.getNorth()),
        Placeholder.parsed("format-northeast", element.getNorthEast()),
        Placeholder.parsed("format-east", element.getEast()),
        Placeholder.parsed("format-southeast", element.getSouthEast()),
        Placeholder.parsed("format-south", element.getSouth()),
        Placeholder.parsed("format-southwest", element.getSouthWest()),
        Placeholder.parsed("format-west", element.getWest()),
        Placeholder.parsed("format-northwest", element.getNorthWest()),
        Placeholder.parsed("format-distance", element.getDistanceFormat())
    );
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    for (AbstractVisualizer.Property<PlaceholderVisualizer, String> prop : PlaceholderVisualizer.PROPS) {
      tree = tree.then(subCommand(prop.getKey(), Arguments.miniMessageArgument("format",
          Objects.equals(prop.getKey(), PlaceholderVisualizer.PROP_DISTANCE.getKey())
              ? i -> Set.of("distance") : i -> new HashSet<>()), prop));
    }
    return tree;
  }

  @Override
  public Map<String, Object> serialize(PlaceholderVisualizer visualizer) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    Arrays.stream(PlaceholderVisualizer.PROPS)
        .forEach(prop -> serialize(map, prop, visualizer));
    return map;
  }

  @Override
  public void deserialize(PlaceholderVisualizer visualizer, Map<String, Object> values) {
    Arrays.stream(PlaceholderVisualizer.PROPS)
        .forEach(prop -> loadProperty(values, visualizer, prop));
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
