package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.module.DiscoverHandler;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.modifier.*;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.implementation.CommonStorage;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.pathfinder.visualizer.impl.CombinedVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.CompassVisualizerType;
import de.cubbossa.pathfinder.visualizer.impl.InternalVisualizerStorage;
import de.cubbossa.pathfinder.visualizer.impl.ParticleVisualizerType;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.PluginTranslations;
import de.cubbossa.translations.Translations;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Locale;
import java.util.Set;

@Getter
public abstract class CommonPathFinder implements PathFinder {

  private static final NamespacedKey GLOBAL_GROUP_KEY = pathfinder("global");
  private static final NamespacedKey DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer");
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
  private EventDispatcher<?> eventDispatcher;
  private PluginTranslations translations;


  public static NamespacedKey globalGroupKey() {
    return GLOBAL_GROUP_KEY;
  }

  public static NamespacedKey defaultVisualizerKey() {
    return DEFAULT_VISUALIZER_KEY;
  }

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  @SneakyThrows
  public void onLoad() {
    PathFinderProvider.setPathFinder(this);

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerHandler();
    modifierRegistry = new ModifierRegistryImpl();

    storage = new StorageImpl(nodeTypeRegistry);

    modifierRegistry.registerModifierType(new PermissionModifierType());
    modifierRegistry.registerModifierType(new NavigableModifierType());
    modifierRegistry.registerModifierType(new DiscoverableModifierType());
    modifierRegistry.registerModifierType(new FindDistanceModifierType());
    modifierRegistry.registerModifierType(new CurveLengthModifierType());
    modifierRegistry.registerModifierType(new VisualizerModifierType());

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    commandRegistry = new CommandRegistry(this);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();

    YamlUtils.registerClasses();
    loadConfig();

    commandRegistry.loadCommands();
    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {
    effectsFile = new File(getDataFolder(), "effects.nbo");

    audiences = provideAudiences();
    Messages.setAudiences(audiences);
    miniMessage = MiniMessage.miniMessage();

    // Data
    new ExamplesHandler(getLogger()).fetchExamples();

    translations = Translations.builder("PathFinder")
        .withDefaultLocale(Locale.forLanguageTag(configuration.language.fallbackLanguage))
        .withEnabledLocales(Locale.getAvailableLocales())
        .withPreferClientLanguage()
        .withLogger(getLogger())
        .withPropertiesStorage(new File(getDataFolder(), "lang"))
        .build();
    translations.addMessagesClass(Messages.class);
    translations.writeLocale(Locale.ENGLISH);

    translations.addStyle("main", Style.style(TextColor.color(0x6569EB)));
    translations.addStyle("main_light", Style.style(TextColor.color(0xA5A7F3)));
    translations.addStyle("main_dark", Style.style(TextColor.color(0x383EE5)));

    translations.addStyle("warm", Style.style(TextColor.color(0xE5D4C0)));
    translations.addStyle("empty", Style.style(TextColor.color(0x554640)));
    translations.addStyle("warn", Style.style(NamedTextColor.YELLOW));
    translations.addStyle("negative", Style.style(NamedTextColor.RED));


    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = switch (configuration.database.type) {
      case SQLITE -> new SqliteStorage(configuration.database.embeddedSql.file, nodeTypeRegistry,
          modifierRegistry, visualizerTypeRegistry);
      case REMOTE_SQL -> new RemoteSqlStorage(configuration.database.remoteSql, nodeTypeRegistry,
          modifierRegistry, visualizerTypeRegistry);
      default -> null;
//      default -> new YmlStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry);
    };
    impl.setLogger(getLogger());
    // TODO bad style
    if (impl instanceof CommonStorage cm) {
      cm.setStorage(storage);
    }
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

    ParticleVisualizerType particleVisualizerType = new ParticleVisualizerType(pathfinder("particle"));
    Set.<AbstractVisualizerType<?>>of(
        particleVisualizerType,
        new CombinedVisualizerType(pathfinder("combined")),
        new CompassVisualizerType(pathfinder("compass"))
    ).forEach(vt -> {
      visualizerTypeRegistry.registerVisualizerType(vt);
      if (impl instanceof InternalVisualizerDataStorage visStorage) {
        vt.setStorage(new InternalVisualizerStorage(vt, visStorage));
      }
    });

    new NodeHandler(this);
    new DiscoverHandler(this);

    commandRegistry.enableCommands(this);
    extensionRegistry.enableExtensions(this);

    storage.createGlobalNodeGroup(particleVisualizerType).join();
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

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerHandler.getInstance();
  }

  abstract AudienceProvider provideAudiences();

  abstract EventDispatcher provideEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
