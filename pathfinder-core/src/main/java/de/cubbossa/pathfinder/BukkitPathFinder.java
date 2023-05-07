package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.events.BukkitEventDispatcher;
import de.cubbossa.pathfinder.listener.PlayerListener;
import lombok.Getter;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class BukkitPathFinder extends CommonPathFinder {

  @Getter
  private static BukkitPathFinder instance;
  private BStatsLoader bstatsLoader;

  public static NamespacedKey convert(org.bukkit.NamespacedKey key) {
    return new NamespacedKey(key.getNamespace(), key.getKey());
  }

  public static PathPlayer<Player> wrap(Player player) {
    return new BukkitPathPlayer(player.getUniqueId());
  }

  private final JavaPlugin javaPlugin;

  public BukkitPathFinder(JavaPlugin javaPlugin) {
    instance = this;
    this.javaPlugin = javaPlugin;
  }

  @Override
  public void onLoad() {
    super.onLoad();
    bstatsLoader = new BStatsLoader(this);
  }

  @Override
  public void onEnable() {
    super.onEnable();
    bstatsLoader.registerStatistics(javaPlugin);

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), javaPlugin);
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }

  @Override
  public File getDataFolder() {
    return javaPlugin.getDataFolder();
  }

  @Override
  public ClassLoader getClassLoader() {
    return javaPlugin.getClass().getClassLoader();
  }

  @Override
  AudienceProvider provideAudiences() {
    return BukkitAudiences.create(javaPlugin);
  }

  @Override
  EventDispatcher provideEventDispatcher() {
    return new BukkitEventDispatcher(getLogger());
  }

  @Override
  void saveResource(String name, boolean override) {
    javaPlugin.saveResource(name, override);
  }

  @Override
  public Logger getLogger() {
    return javaPlugin.getLogger();
  }

  @Override
  public void runSynchronized(Runnable runnable) {
    Bukkit.getScheduler().runTask(javaPlugin, runnable);
  }

  @Override
  public String getVersion() {
    return javaPlugin.getDescription().getVersion();
  }
}
