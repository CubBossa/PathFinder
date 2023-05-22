package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.*;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.ExamplesLoader;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.storage.implementation.YmlStorage;
import de.cubbossa.pathfinder.util.CommonLocationWeightSolverRegistry;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.GlobalTranslations;
import de.cubbossa.translations.MessageBundle;
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
import java.util.UUID;

@Getter
public abstract class CommonPathFinder implements PathFinder {

  private static CommonPathFinder instance;

  public static CommonPathFinder getInstance() {
    return instance;
  }

  private static final NamespacedKey GLOBAL_GROUP_KEY = pathfinder("global");
  private static final NamespacedKey DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer");
  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  protected NodeTypeRegistry nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected ExtensionsRegistry extensionRegistry;
  protected ConfigFileLoader configFileLoader;
  protected AudienceProvider audiences;
  protected MiniMessage miniMessage;
  protected File effectsFile;
  protected StorageImpl storage;
  protected LocationWeightSolverRegistry<Node> locationWeightSolverRegistry;
  @Setter
  protected PathFinderConf configuration;
  protected EventDispatcher<?> eventDispatcher;
  protected MessageBundle translations;


  public static NamespacedKey globalGroupKey() {
    return GLOBAL_GROUP_KEY;
  }

  public static NamespacedKey defaultVisualizerKey() {
    return DEFAULT_VISUALIZER_KEY;
  }

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  public abstract World getWorld(UUID worldId);

  public abstract <PlayerT> PathPlayer<PlayerT> wrap(UUID playerId);

  public abstract <PlayerT> PathPlayer<PlayerT> wrap(PlayerT player);

  @SneakyThrows
  public void onLoad() {
    instance = this;
    PathFinderProvider.setPathFinder(this);

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerHandler();
    modifierRegistry = new ModifierRegistryImpl();
    locationWeightSolverRegistry = new CommonLocationWeightSolverRegistry<>();

    storage = new StorageImpl(nodeTypeRegistry);

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();

    loadConfig();

    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {
    effectsFile = new File(getDataFolder(), "effects.nbo");

    audiences = provideAudiences();
    Messages.setAudiences(audiences);
    miniMessage = MiniMessage.miniMessage();

    // Data
    new ExamplesLoader(visualizerTypeRegistry).getExampleFiles();

    translations = GlobalTranslations.builder("PathFinder")
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


    translations.addStyle("offset", Style.style(TextColor.color(0x7B42F5)));
    translations.addStyle("offset_light", Style.style(TextColor.color(0xAE8BF9)));
    translations.addStyle("offset_dark", Style.style(TextColor.color(0x383EE5)));
    translations.addStyle("accent", Style.style(TextColor.color(0xF26419)));
    translations.addStyle("accent_light", Style.style(TextColor.color(0xF58B51)));
    translations.addStyle("accent_dark", Style.style(TextColor.color(0xC14B0B)));
    translations.addStyle("bg", Style.style(NamedTextColor.GRAY));
    translations.addStyle("bg_light", Style.style(NamedTextColor.WHITE));
    translations.addStyle("bg_dark", Style.style(NamedTextColor.DARK_GRAY));

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
      default -> new YmlStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry,
          visualizerTypeRegistry, modifierRegistry);
    };
    impl.setWorldLoader(this::getWorld);
    impl.setLogger(getLogger());

    storage.setImplementation(impl);
    storage.setEventDispatcher(eventDispatcher);
    storage.setLogger(getLogger());
    storage.init();

    nodeTypeRegistry.register(new WaypointType(
        new WaypointStorage(storage),
        miniMessage
    ));

    new NodeHandler(this);
    extensionRegistry.enableExtensions(this);
  }

  @SneakyThrows
  public void onDisable() {
    NodeHandler.getInstance().cancelAllEditModes();
    extensionRegistry.disableExtensions(this);
    storage.shutdown();
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
