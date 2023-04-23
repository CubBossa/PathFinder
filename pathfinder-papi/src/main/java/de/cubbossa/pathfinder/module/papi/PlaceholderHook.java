package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

  public static final AbstractVisualizerType<PlaceholderVisualizer> PLACEHOLDER_VISUALIZER_TYPE =
      new PlaceholderVisualizerType(PathPlugin.pathfinder("placeholder"));

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
