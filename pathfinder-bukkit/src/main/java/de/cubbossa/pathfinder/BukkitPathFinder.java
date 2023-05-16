package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Task;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathfinder.events.BukkitEventDispatcher;
import de.cubbossa.pathfinder.listener.BukkitEffects;
import de.cubbossa.pathfinder.listener.PlayerListener;
import de.cubbossa.pathfinder.module.BukkitDiscoverHandler;
import de.cubbossa.pathfinder.nodegroup.modifier.*;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.util.BukkitMainThreadExecutor;
import de.cubbossa.pathfinder.util.CommonLocationWeightSolverRegistry;
import de.cubbossa.pathfinder.util.WorldImpl;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.pathfinder.util.location.LocationWeightSolverPreset;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.CombinedVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.CompassVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.InternalVisualizerStorage;
import de.cubbossa.pathfinder.visualizer.impl.ParticleVisualizerType;
import lombok.Getter;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

@Getter
public class BukkitPathFinder extends CommonPathFinder {

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

    modifierRegistry.registerModifierType(new PermissionModifierType());
    modifierRegistry.registerModifierType(new NavigableModifierType());
    modifierRegistry.registerModifierType(new DiscoverableModifierType());
    modifierRegistry.registerModifierType(new FindDistanceModifierType());
    modifierRegistry.registerModifierType(new CurveLengthModifierType());
    modifierRegistry.registerModifierType(new VisualizerModifierType());
  }

  @Override
  public void onEnable() {
    super.onEnable();
    bstatsLoader.registerStatistics(javaPlugin);

    new BukkitDiscoverHandler(this);

    ParticleVisualizerType particleVisualizerType = new ParticleVisualizerType(pathfinder("particle"));

    Set.<AbstractVisualizerType<?>>of(
        particleVisualizerType,
        new CompassVisualizerType(pathfinder("compass")),
        new CombinedVisualizerType(pathfinder("combined"))
    ).forEach(vt -> {
      getVisualizerTypeRegistry().registerVisualizerType(vt);
      if (getStorage().getImplementation() instanceof InternalVisualizerDataStorage visStorage) {
        vt.setStorage(new InternalVisualizerStorage(vt, visStorage));
      }
    });

    commandRegistry.enableCommands(this);
    getStorage().createGlobalNodeGroup(particleVisualizerType);

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), javaPlugin);

    locationWeightSolverRegistry.register(CommonLocationWeightSolverRegistry.KEY_SIMPLE, () -> LocationWeightSolverPreset.SIMPLE.getSolverFunction().apply(configuration.navigation.nearestLocationSolver.simpleConfig));
    locationWeightSolverRegistry.register(CommonLocationWeightSolverRegistry.KEY_RAYCAST, () -> LocationWeightSolverPreset.RAYCAST.getSolverFunction().apply(configuration.navigation.nearestLocationSolver.raycastConfig));

    new BukkitEffects((EventDispatcher<Player>) eventDispatcher, configuration.effects);
  }

  @Override
  public void onDisable() {
    super.onDisable();
    commandRegistry.unregisterCommands();
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
    throw new IllegalStateException("One implementation of CommonPathFinder must only use one type of Task representation");
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
  public String getVersion() {
    return javaPlugin.getDescription().getVersion();
  }
}
