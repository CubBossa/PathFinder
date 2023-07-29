package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.dump.DumpWriter;
import de.cubbossa.pathapi.dump.DumpWriterProvider;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PathFinderPlugin extends JavaPlugin {

  @Getter
  private static PathFinderPlugin instance;

  private final BukkitPathFinder pathFinder;

  public PathFinderPlugin() {
    instance = this;
    pathFinder = new BukkitPathFinder(this);
  }

  @Override
  public void onLoad() {
    pathFinder.onLoad();

    DumpWriter dumpWriter = DumpWriterProvider.get();
    dumpWriter.addProperty("mc-version", () -> getServer().getVersion());
    dumpWriter.addProperty("bukkit-version", () -> getServer().getBukkitVersion());
    dumpWriter.addProperty("active-plugins", () -> Arrays.stream(getServer().getPluginManager().getPlugins())
        .map(Plugin::getName).toList());
  }

  @Override
  public void onEnable() {
    pathFinder.onEnable();
  }

  @Override
  public void onDisable() {
    pathFinder.onDisable();
  }
}
