package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.commands.CustomArgs;
import de.cubbossa.pathfinder.commands.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.commands.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Getter
@Setter
public class PlaceholderVisualizerType extends AbstractVisualizerType<PlaceholderVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<PlaceholderVisualizer> {

  public PlaceholderVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public PlaceholderVisualizer create(NamespacedKey key, String nameFormat) {
    return new PlaceholderVisualizer(key, nameFormat);
  }

  @Override
  public Message getInfoMessage(PlaceholderVisualizer element) {
    return Messages.CMD_VIS_PAPI_INFO.format(TagResolver.builder()
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
}
