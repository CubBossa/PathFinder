package de.cubbossa.pathfinder;

import de.cubbossa.disposables.Disposer;
import de.cubbossa.disposablesbukkit.BukkitDisposer;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Task;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathfinder.events.BukkitEventDispatcher;
import de.cubbossa.pathfinder.listener.BukkitEffects;
import de.cubbossa.pathfinder.listener.PlayerListener;
import de.cubbossa.pathfinder.migration.V5_0_0__Config;
import de.cubbossa.pathfinder.node.NodeSelectionProviderImpl;
import de.cubbossa.pathfinder.node.selection.BukkitNodeSelectionParser;
import de.cubbossa.pathfinder.util.BukkitMainThreadExecutor;
import de.cubbossa.pathfinder.util.WorldImpl;
import de.cubbossa.pathfinder.util.YamlUtils;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import lombok.Getter;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class BukkitPathFinder extends AbstractPathFinder {

  @Getter
  private static BukkitPathFinder instance;
  private static BukkitMainThreadExecutor executor;
  private BStatsLoader bstatsLoader;
  private CommandRegistry commandRegistry;

  public static Executor mainThreadExecutor() {
    if (executor == null) {
      JavaPlugin plugin = PathFinderPlugin.getInstance();
      if (plugin == null) {
        throw new IllegalStateException("Aquired main thread executor before plugin was loaded");
      }
      executor = new BukkitMainThreadExecutor(plugin);
    }
    return executor;
  }

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
  public World getWorld(UUID worldId) {
    return new WorldImpl(worldId);
  }

  @Override
  public <PlayerT> PathPlayer<PlayerT> wrap(UUID playerId) {
    return (PathPlayer<PlayerT>) new BukkitPathPlayer(playerId);
  }

  @Override
  public <PlayerT> PathPlayer<PlayerT> wrap(PlayerT player) {
    if (player instanceof Player bukkitPlayer) {
      return (PathPlayer<PlayerT>) new BukkitPathPlayer(bukkitPlayer.getUniqueId());
    } else if (player instanceof ConsoleCommandSender) {
      return (PathPlayer<PlayerT>) new BukkitPathSender();
    }
    throw new IllegalStateException("Illegal player type '" + player.getClass() + "'.");
  }

  @Override
  public void onLoad() {
    commandRegistry = new CommandRegistry(this);
    commandRegistry.loadCommands();
    super.onLoad();
    bstatsLoader = new BStatsLoader(this);
    YamlUtils.registerClasses();

    new NodeSelectionProviderImpl<>(new BukkitNodeSelectionParser("n"));
  }

  @Override
  public void onEnable() {
    super.onEnable();
    bstatsLoader.registerStatistics(javaPlugin);

    commandRegistry.enableCommands(this);

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), javaPlugin);

    new BukkitEffects((EventDispatcher<Player>) eventDispatcher, ((PathFinderConfigImpl) configuration).effects);
  }

  @Override
  public void dispose() {
    instance = null;
    super.dispose();
  }

  public void onDisable() {
    super.shutdown();
  }

  @Override
  public File getDataFolder() {
    return javaPlugin.getDataFolder();
  }

  @Override
  public ClassLoader getClassLoader() {
    return javaPlugin.getClass().getClassLoader();
  }

  record BukkitTaskWrapper(BukkitTask task) implements Task {
  }

  @Override
  public Task repeatingTask(Runnable runnable, long delay, long interval) {
    BukkitTask task = Bukkit.getScheduler().runTaskTimer(javaPlugin, runnable, delay, interval);
    return new BukkitTaskWrapper(task);
  }

  @Override
  public void cancelTask(Task task) {
    if (task instanceof BukkitTaskWrapper bukkitTaskWrapper) {
      bukkitTaskWrapper.task().cancel();
      return;
    }
    throw new IllegalStateException("One implementation of AbstractPathFinder must only use one type of Task representation");
  }

  @Override
  Disposer createDisposer() {
    return BukkitDisposer.disposer(javaPlugin);
  }

  @Override
  AudienceProvider createAudiences() {
    return BukkitAudiences.create(javaPlugin);
  }

  @Override
  EventDispatcher<?> createEventDispatcher() {
    return new BukkitEventDispatcher(getLogger());
  }

  @Override
  void saveResource(String name, boolean override) {
    if (override || !new File(getDataFolder(), name).exists()) {
      javaPlugin.saveResource(name, override);
    }
  }

  @Override
  public Logger getLogger() {
    return javaPlugin.getLogger();
  }

  @Override
  public String getVersion() {
    return javaPlugin.getDescription().getVersion();
  }

  @Override
  public Object[] getMigrations() {
    return new Object[] {
        new V5_0_0__Config()
    };
  }
}
