package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.dump.DumpWriter;
import de.cubbossa.pathapi.dump.DumpWriterProvider;
import de.cubbossa.tinytranslations.BukkitTinyTranslations;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
        .map(Plugin::getName).collect(Collectors.toCollection(TreeSet::new)));
    dumpWriter.addProperty("worlds", () -> Bukkit.getWorlds().stream()
        .collect(Collectors.toMap(World::getUID, World::getName, (s, s2) -> s, TreeMap::new)));
  }

  @Override
  public void onEnable() {
    pathFinder.translations = BukkitTinyTranslations.application(this);
    pathFinder.onEnable();
  }

  @Override
  public void onDisable() {
    pathFinder.onDisable();
  }
}
