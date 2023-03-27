package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.PathPluginExtension;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PlaceholderExtension implements PathPluginExtension {

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return NamespacedKey.fromString("pathfinder:papi");
  }

  @Override
  public void onEnable(PathPlugin pathPlugin) {
    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
    if (papi == null) {
      pathPlugin.getExtensionsRegistry().unregisterExtension(this);
    }
    pathPlugin.getLogger().log(Level.INFO, "Found PlaceholderAPI, registered module.");

    PlaceholderHook hook = new PlaceholderHook(pathPlugin);
    VisualizerHandler.getInstance().registerVisualizerType(PlaceholderHook.PLACEHOLDER_VISUALIZER_TYPE);
  }
}
