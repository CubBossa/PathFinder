package de.cubbossa.pathfinder.hook;

import de.cubbossa.pathfinder.Dependency;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PlaceholderVisualizer;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion implements Dependency {

  public static final VisualizerType<PlaceholderVisualizer> PLACEHOLDER_VISUALIZER_TYPE =
      new VisualizerType<>(
          new NamespacedKey(PathPlugin.getInstance(), "placeholderapi")
      ) {
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
      };

  public static final String DIRECTION = "direction";
  public static final String DISTANCE = "distance";

  @Getter
  private static PlaceholderHook instance;
  private final PathPlugin plugin;

  private final Map<String, Map<OfflinePlayer, Supplier<String>>> resolvers = new HashMap<>();

  public PlaceholderHook(PathPlugin plugin) {
    instance = this;
    this.plugin = plugin;

    register();
    VisualizerHandler.getInstance().registerVisualizerType(PLACEHOLDER_VISUALIZER_TYPE);
  }

  public void register(String key, OfflinePlayer player, Supplier<String> placeholder) {
    resolvers.computeIfAbsent(key, s -> new HashMap<>()).put(player, placeholder);
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public @NotNull String getIdentifier() {
    return plugin.getName();
  }

  @Override
  public @NotNull String getAuthor() {
    return String.join(", ", plugin.getDescription().getAuthors());
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
    if (player == null) {
      return "";
    }
    if (params.equals("")) {
      return "";
    }
    var map = resolvers.get(params);
    if (map == null) {
      // unknown placeholder
      return null;
    }
    return map.getOrDefault(player, () -> "").get();
  }
}
