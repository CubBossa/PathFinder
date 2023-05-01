package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.DatabaseType;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.modifier.CurveLengthModifierType;
import de.cubbossa.pathfinder.modifier.DiscoverableModifierType;
import de.cubbossa.pathfinder.modifier.FindDistanceModifierType;
import de.cubbossa.pathfinder.modifier.NavigableModifierType;
import de.cubbossa.pathfinder.modifier.PermissionModifierType;
import de.cubbossa.pathfinder.module.DiscoverHandler;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.pathfinder.visualizer.impl.CombinedVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.CompassVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.ParticleVisualizerType;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.TranslationHandler;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Getter
public abstract class CommonPathFinder implements PathFinder {

  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  private NodeTypeRegistry nodeTypeRegistry;
  private VisualizerTypeRegistry visualizerTypeRegistry;
  private ModifierRegistry modifierRegistry;
  private ExtensionsRegistry extensionRegistry;
  private CommandRegistry commandRegistry;
  private ConfigFileLoader configFileLoader;
  private AudienceProvider audiences;
  private MiniMessage miniMessage;
  private File effectsFile;
  private StorageImpl storage;
  @Setter
  private PathFinderConf configuration;
  private EventDispatcher eventDispatcher;

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  @SneakyThrows
  public void onLoad() {
    PathFinderProvider.setPathFinder(this);

    storage = new StorageImpl();

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerHandler();
    modifierRegistry = new ModifierRegistryImpl();

    modifierRegistry.registerModifierType(new PermissionModifierType());
    modifierRegistry.registerModifierType(new NavigableModifierType());
    modifierRegistry.registerModifierType(new DiscoverableModifierType());
    modifierRegistry.registerModifierType(new FindDistanceModifierType());
    modifierRegistry.registerModifierType(new CurveLengthModifierType());

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    commandRegistry = new CommandRegistry(this);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();

    generateIfAbsent("lang/styles.yml");
    generateIfAbsent("lang/de_DE.yml");

    YamlUtils.registerClasses();
    loadConfig();

    commandRegistry.loadCommands();
    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {
    effectsFile = new File(getDataFolder(), "effects.nbo");

    audiences = provideAudiences();
    miniMessage = MiniMessage.miniMessage();

    // Data
    new ExamplesHandler(getLogger()).fetchExamples();

    TranslationHandler translationHandler =
        new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"));
    translationHandler.registerAnnotatedLanguageClass(Messages.class);
    translationHandler.setFallbackLanguage(configuration.language.fallbackLanguage);
    translationHandler.setUseClientLanguage(configuration.language.clientLanguage);
    translationHandler.loadStyle();
    translationHandler.loadLanguages();


    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = switch (DatabaseType.valueOf(configuration.database.type)) {
      case SQLITE -> new SqliteStorage(configuration.database.embeddedSql.file, nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
      case REMOTE_SQL -> new RemoteSqlStorage(configuration.database.remoteSql, nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
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

    nodeTypeRegistry.register(new WaypointType(
            new WaypointStorage(storage),
            miniMessage
    ));

    visualizerTypeRegistry.registerVisualizerType(new ParticleVisualizerType(CommonPathFinder.pathfinder("particle")));
    visualizerTypeRegistry.registerVisualizerType(new CombinedVisualizerType(CommonPathFinder.pathfinder("combined")));
    visualizerTypeRegistry.registerVisualizerType(new CompassVisualizerType(CommonPathFinder.pathfinder("compass")));
    new NodeHandler(this);
    new DiscoverHandler(this);

    commandRegistry.enableCommands(this);
    extensionRegistry.enableExtensions(this);
  }

  @SneakyThrows
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

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerHandler.getInstance();
  }

  abstract AudienceProvider provideAudiences();

  abstract EventDispatcher provideEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
