package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.events.BukkitEventDispatcher;
import de.cubbossa.pathfinder.listener.PlayerListener;
import de.cubbossa.pathfinder.module.DiscoverHandler;
import de.cubbossa.pathfinder.node.AbstractNodeType;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.modifier.CurveLengthModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.NavigableModifierType;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifierType;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.PathPlayerImpl;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
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
  @Getter
  private static PathPlugin instance;
  private final NodeTypeRegistryImpl nodeTypeRegistry;
  private final ModifierRegistryImpl modifierRegistry;
  private final ExtensionsRegistry extensionRegistry;
  private final CommandRegistry commandRegistry;
  private final BStatsLoader bstatsLoader;
  private final ConfigFileLoader configFileLoader;
  private BukkitAudiences audiences;
  private MiniMessage miniMessage;
  private File effectsFile;
  private StorageImpl storage;
  @Setter
  private PathPluginConfig configuration;
  private EventDispatcher eventDispatcher;

  public PathPlugin() {
    instance = this;
    PathFinderProvider.setPathFinder(this);

    storage = new StorageImpl();

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();

    modifierRegistry.registerModifierType(new PermissionModifierType());
    modifierRegistry.registerModifierType(new NavigableModifierType());
    modifierRegistry.registerModifierType(new DiscoverableModifierType());
    modifierRegistry.registerModifierType(new FindDistanceModifierType());
    modifierRegistry.registerModifierType(new CurveLengthModifierType());

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    bstatsLoader = new BStatsLoader();
    commandRegistry = new CommandRegistry(this);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = new BukkitEventDispatcher(getLogger());
  }

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  public static NamespacedKey convert(org.bukkit.NamespacedKey key) {
    return new NamespacedKey(key.getNamespace(), key.getKey());
  }

  public static PathPlayer<Player> wrap(Player player) {
    return new PathPlayerImpl(player.getUniqueId());
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
      case SQLITE -> new SqliteStorage(configuration.database.embeddedSql.file, nodeTypeRegistry,
          modifierRegistry);
      case REMOTE_SQL -> new RemoteSqlStorage(configuration.database.remoteSql, nodeTypeRegistry,
          modifierRegistry);
      default -> null;
//      default -> new YmlStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry);
    };
    impl.setLogger(getLogger());
    storage.setImplementation(impl);
    storage.setEventDispatcher(eventDispatcher);
    storage.setLogger(getLogger());
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
    nodeTypeRegistry.setWaypointNodeType(nodeType);
    nodeTypeRegistry.register(nodeType);
  }

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerHandler.getInstance();
  }
}
