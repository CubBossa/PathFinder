package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

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

        PlaceholderHook hook = new PlaceholderHook(JavaPlugin.getPlugin(CommonPathFinder.class));
    VisualizerHandler.getInstance()
        .registerVisualizerType(PlaceholderHook.PLACEHOLDER_VISUALIZER_TYPE);
  }
}
