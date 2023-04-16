package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.api.EventDispatcher;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderProvider;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.misc.Vector;
import de.cubbossa.pathfinder.api.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.core.ExamplesHandler;
import de.cubbossa.pathfinder.core.events.BukkitEventDispatcher;
import de.cubbossa.pathfinder.core.listener.PlayerListener;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.AbstractNodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.core.node.WaypointType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.core.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.core.nodegroup.modifier.PermissionModifierType;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.storage.Storage;
import de.cubbossa.pathfinder.api.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.PathPlayerImpl;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.TranslationHandler;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PathPlugin extends JavaPlugin implements PathFinder {

  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  public static NamespacedKey convert(org.bukkit.NamespacedKey key) {
    return new NamespacedKey(key.getNamespace(), key.getKey());
  }

  public static PathPlayer<Player> wrap(Player player) {
    return new PathPlayerImpl(player.getUniqueId());
  }

  @Getter
  private static PathPlugin instance;

  private BukkitAudiences audiences;
  private MiniMessage miniMessage;

  private File effectsFile;
  private final NodeTypeRegistry nodeTypeRegistry;
  private final ModifierRegistryImpl modifierRegistry;
  private Storage storage;
  @Setter
  private PathPluginConfig configuration;
  private final ExtensionsRegistry extensionRegistry;
  private final CommandRegistry commandRegistry;
  private final BStatsLoader bStatsLoader;
  private final ConfigFileLoader configFileLoader;
  private EventDispatcher eventDispatcher;

  private de.cubbossa.pathfinder.api.node.NodeType<Waypoint> waypointNodeType;

  public PathPlugin() {
    instance = this;
    PathFinderProvider.setPathFinder(this);

    storage = new Storage(this);

    nodeTypeRegistry = new NodeTypeRegistry();
    modifierRegistry = new ModifierRegistryImpl();

    modifierRegistry.registerModifierType(new PermissionModifierType());

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    bStatsLoader = new BStatsLoader();
    commandRegistry = new CommandRegistry(this);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = new BukkitEventDispatcher();
  }

  @SneakyThrows
  @Override
  public void onLoad() {

    generateIfAbsent("lang/styles.yml");
    generateIfAbsent("lang/de_DE.yml");

    YamlUtils.registerClasses();
    loadConfig();

    commandRegistry.loadCommands();
    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  @Override
  public void onEnable() {
    effectsFile = new File(getDataFolder(), "effects.nbo");

    audiences = BukkitAudiences.create(this);
    miniMessage = MiniMessage.miniMessage();

    // Data
    new ExamplesHandler().fetchExamples();

    TranslationHandler translationHandler =
        new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"));
    translationHandler.registerAnnotatedLanguageClass(Messages.class);
    translationHandler.setFallbackLanguage(configuration.language.fallbackLanguage);
    translationHandler.setUseClientLanguage(configuration.language.clientLanguage);
    translationHandler.loadStyle();
    translationHandler.loadLanguages();


    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = switch (configuration.database.type) {
      case SQLITE -> new SqliteStorage(this, configuration.database.embeddedSql.file, nodeTypeRegistry);
      case REMOTE_SQL -> new RemoteSqlStorage(this, configuration.database.remoteSql, nodeTypeRegistry);
      default -> null;
//      default -> new YmlStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry);
    };
    storage.setImplementation(impl);
    storage.init();

    ExamplesHandler examples = ExamplesHandler.getInstance();
    examples.afterFetch(() -> {
      examples.getExamples().forEach(examples::loadVisualizer);
    });

    setWaypointNodeType(new WaypointType(
        new WaypointStorage(storage),
        miniMessage
    ));

    if (!effectsFile.exists()) {
      saveResource("effects.nbo", false);
    }
    new EffectHandler(this, TranslationHandler.getInstance().getAudiences(),
        TranslationHandler.getInstance().getMiniMessage(),
        context -> TranslationHandler.getInstance()
            .translateLine(context.text(), context.player(), context.resolver()));

    new VisualizerHandler();
    new NodeHandler(this);
    new DiscoverHandler(this);

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

    commandRegistry.enableCommands(this);
    extensionRegistry.enableExtensions(this);
  }

  @SneakyThrows
  @Override
  public void onDisable() {

    NodeHandler.getInstance().cancelAllEditModes();

    extensionRegistry.disableExtensions(this);

    storage.shutdown();
    commandRegistry.unregisterCommands();
  }

  public void loadConfig() {
    configuration = configFileLoader.loadConfig();
  }

  public void generateIfAbsent(String resource) {
    if (!new File(getDataFolder(), resource).exists()) {
      saveResource(resource, false);
    }
  }

  public void setWaypointNodeType(AbstractNodeType<Waypoint> nodeType) {
    waypointNodeType = nodeType;
    nodeTypeRegistry.setWaypointNodeType(nodeType);
    nodeTypeRegistry.register(nodeType);
  }

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerHandler.getInstance();
  }
}
