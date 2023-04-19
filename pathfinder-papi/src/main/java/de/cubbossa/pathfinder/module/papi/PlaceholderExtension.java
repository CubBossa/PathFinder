package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderExtension;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlaceholderExtension implements PathFinderExtension {

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return NamespacedKey.fromString("pathfinder:papi");
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
    if (papi == null) {
      pathPlugin.getExtensionRegistry().unregisterExtension(this);
    }
    pathPlugin.getLogger().log(Level.INFO, "Found PlaceholderAPI, registered module.");

    PlaceholderHook hook = new PlaceholderHook(JavaPlugin.getPlugin(PathPlugin.class));
    VisualizerHandler.getInstance().registerVisualizerType(PlaceholderHook.PLACEHOLDER_VISUALIZER_TYPE);
  }
}
