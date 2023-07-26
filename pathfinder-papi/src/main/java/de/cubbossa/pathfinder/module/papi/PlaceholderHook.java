package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlaceholderHook extends PlaceholderExpansion {

  public static final AbstractVisualizerType<PlaceholderVisualizer> PLACEHOLDER_VISUALIZER_TYPE =
      new PlaceholderVisualizerType(CommonPathFinder.pathfinder("placeholder"));

  public static final String DIRECTION = "direction";
  public static final String DISTANCE = "distance";

  @Getter
  private static PlaceholderHook instance;
  private final PathFinderPlugin plugin;

  private final Map<String, Map<OfflinePlayer, Supplier<String>>> resolvers = new HashMap<>();

  public PlaceholderHook(PathFinderPlugin plugin) {
    instance = this;
    this.plugin = plugin;

    register();
    VisualizerTypeRegistryImpl.getInstance().registerVisualizerType(PLACEHOLDER_VISUALIZER_TYPE);
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
