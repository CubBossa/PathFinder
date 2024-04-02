package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.visualizer.PathFinderExtensionBase;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlaceholderExtension extends PathFinderExtensionBase implements PathFinderExtension {

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return NamespacedKey.fromString("pathfinder:papi");
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
    if (papi == null) {
      disable();
    }
    pathPlugin.getLogger().log(Level.INFO, "Found PlaceholderAPI, registered module.");

    PlaceholderHook hook = new PlaceholderHook(JavaPlugin.getPlugin(PathFinderPlugin.class));
    pathPlugin.getDisposer().register(this, hook);

    VisualizerTypeRegistryImpl.getInstance()
        .registerVisualizerType(PlaceholderHook.PLACEHOLDER_VISUALIZER_TYPE);
  }
}
