package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.storage.implementation.YmlStorage;
import de.cubbossa.pathfinder.storage.v3.V3Converter;
import de.cubbossa.pathfinder.storage.v3.V3SqliteStorage;
import de.cubbossa.pathfinder.storage.v3.V3YmlStorage;
import de.cubbossa.pathfinder.util.FileUtils;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.GlobalMessageBundle;
import de.cubbossa.translations.MessageBundle;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public abstract class CommonPathFinder implements PathFinder {

  private static CommonPathFinder instance;

  public static CommonPathFinder getInstance() {
    return instance;
  }

  private static final NamespacedKey GLOBAL_GROUP_KEY = pathfinder("global");
  private static final NamespacedKey DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer");
  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  protected ApplicationState state;

  protected NodeTypeRegistry nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected ExtensionsRegistry extensionRegistry;
  protected ConfigFileLoader configFileLoader;
  protected AudienceProvider audiences;
  protected MiniMessage miniMessage;
  protected StorageImpl storage;
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

  @Override
  public void load() {
    if (!(state.equals(ApplicationState.DISABLED) || state.equals(ApplicationState.EXCEPTIONALLY))) {
      throw new IllegalStateException("Trying to load PathFinder - Application already enabled.");
    }
    state = ApplicationState.LOADING;
    onLoad();
  }

  @SneakyThrows
  public void onLoad() {
    instance = this;
    state = ApplicationState.LOADING;
    PathFinderProvider.setPathFinder(this);

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    loadConfig();

    storage = new StorageImpl(nodeTypeRegistry);
    if (!configuration.getDatabase().isCaching()) {
      storage.setCache(null);
    }
    StorageUtil.storage = storage;

    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();

    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {
    miniMessage = MiniMessage.miniMessage();

    audiences = provideAudiences();
    Messages.setAudiences(audiences);

    saveResource("lang/styles.properties", false);

    // Data
    translations = GlobalMessageBundle.applicationTranslationsBuilder("PathFinder", getDataFolder())
            .withDefaultLocale(configuration.language.fallbackLanguage)
        .withEnabledLocales(Locale.getAvailableLocales())
        .withPreferClientLanguage(configuration.language.clientLanguage)
        .withLogger(getLogger())
        .withPropertiesStorage(new File(getDataFolder(), "lang"))
        .withPropertiesStyles(new File(getDataFolder(), "lang/styles.properties"))
        .build();

    miniMessage = MiniMessage.builder()
            .editTags(builder -> builder
                    .resolvers(translations.getBundleResolvers())
                    .resolvers(translations.getStylesResolver())
            )
            .build();

    translations.addMessagesClass(Messages.class);
    translations.writeLocale(Locale.ENGLISH);

    if (configFileLoader.isVersionChange()) {
      File data = new File(getDataFolder(), "data/");
      File oldData = new File(getDataFolder(), "old_data/");
      if (!FileUtils.renameTo(data, oldData)) {
        shutdownExceptionally(new IllegalStateException("Could not store current data directory to old data."));
        return;
      }
    }

    Messages.formatter().setMiniMessage(miniMessage);
    Messages.formatter().setNullStyle(translations.getStyles().get("c-offset-dark"));
    Messages.formatter().setTextStyle(translations.getStyles().get("c-offset"));
    Messages.formatter().setNumberStyle(translations.getStyles().get("c-offset-light"));

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
    setupVisualizerTypes();
    getStorage().createGlobalNodeGroup(visualizerTypeRegistry.getDefaultType()).join();

    nodeTypeRegistry.register(new WaypointType(
            new WaypointStorage(storage),
            miniMessage
    ));

    new NodeHandler(this);
    extensionRegistry.enableExtensions(this);

    if (!configFileLoader.isVersionChange()) {
      state = ApplicationState.RUNNING;
    } else {

      var conv = new V3Converter(
              getLogger(),
              switch (configFileLoader.getOldDatabaseType()) {
                case YML -> new V3YmlStorage(new File(getDataFolder(), "old_data/"));
                default -> new V3SqliteStorage(new File(getDataFolder(), "old_data/database.db"));
              },
              storage,
              nodeTypeRegistry,
              visualizerTypeRegistry,
              this::getWorld
      );
      Thread t = new Thread(conv);
      t.start();

      new Thread(() -> {
        this.getLogger().log(Level.INFO, "Data conversion started.");
        while (t.isAlive() && conv.progress < conv.estimate && conv.exception == null) {
          this.getLogger().log(Level.INFO, "Data conversion progress: " + ((float) conv.progress * 100 / conv.estimate) + "%");
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        if (conv.exception != null) {
          this.getLogger().log(Level.SEVERE, "Error while converting data: ", conv.exception);
          return;
        }
        if (!t.isAlive()) {
          this.getLogger().log(Level.INFO, "Data conversion completed.");
        }
        state = ApplicationState.RUNNING;
      }).start();
    }
  }

  abstract void setupVisualizerTypes();

  @Override
  public void shutdown() {
    if (state.equals(ApplicationState.DISABLED) || state.equals(ApplicationState.EXCEPTIONALLY)) {
      throw new IllegalStateException("Trying to shutdown PathFinder - Application already disabled.");
    }
    state = ApplicationState.DISABLED;

    NodeHandler.getInstance().cancelAllEditModes();
    extensionRegistry.disableExtensions(this);
    storage.shutdown();
  }

  @Override
  public void shutdownExceptionally(Throwable t) {
    shutdown();
    state = ApplicationState.EXCEPTIONALLY;
  }

  public void loadConfig() {
    configuration = configFileLoader.loadConfig();
  }

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerTypeRegistryImpl.getInstance();
  }

  abstract AudienceProvider provideAudiences();

  abstract EventDispatcher<?> provideEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
