package de.cubbossa.pathfinder.module.papi;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
public class PlaceholderVisualizerType extends AbstractVisualizerType<PlaceholderVisualizer>
        implements VisualizerTypeCommandExtension,
        VisualizerTypeMessageExtension<PlaceholderVisualizer> {

    public PlaceholderVisualizerType(NamespacedKey key) {
        super(key);
    }

  @Override
  public PlaceholderVisualizer create(NamespacedKey key) {
    return new PlaceholderVisualizer(key);
  }

  @Override
  public Message getInfoMessage(PlaceholderVisualizer element) {
    return Messages.CMD_VIS_PAPI_INFO.formatted(TagResolver.builder()
        .resolver(Placeholder.parsed("format-north", element.getNorth()))
        .resolver(Placeholder.parsed("format-northeast", element.getNorthEast()))
        .resolver(Placeholder.parsed("format-east", element.getEast()))
        .resolver(Placeholder.parsed("format-southeast", element.getSouthEast()))
        .resolver(Placeholder.parsed("format-south", element.getSouth()))
        .resolver(Placeholder.parsed("format-southwest", element.getSouthWest()))
        .resolver(Placeholder.parsed("format-west", element.getWest()))
        .resolver(Placeholder.parsed("format-northwest", element.getNorthWest()))
        .resolver(Placeholder.parsed("format-distance", element.getDistanceFormat()))
        .build());
  }

  @Override
  public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
                                        int argumentOffset) {
    Arrays.stream(PlaceholderVisualizer.PROPS).forEach(prop -> {
      tree.then(subCommand(prop.getKey(), CustomArgs.miniMessageArgument("format",
          Objects.equals(prop.getKey(), PlaceholderVisualizer.PROP_DISTANCE.getKey())
              ? i -> Set.of("distance") : i -> new HashSet<>()), prop));
    });
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
        setProperty(CommonPathFinder.getInstance().wrap(commandSender), (V) visualizer, property, args.getUnchecked(1));
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
