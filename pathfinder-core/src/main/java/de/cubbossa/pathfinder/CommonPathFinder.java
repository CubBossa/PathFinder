package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.dump.DumpWriter;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.*;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.dump.CommonDumpWriter;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.storage.v3.V3Converter;
import de.cubbossa.pathfinder.storage.v3.V3SqliteStorage;
import de.cubbossa.pathfinder.storage.v3.V3YmlStorage;
import de.cubbossa.pathfinder.util.FileUtils;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
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
  protected StorageImpl storage;
  @Setter
  protected PathFinderConf configuration;
  protected EventDispatcher<?> eventDispatcher;
  protected MessageTranslator translations;
  protected DumpWriter dumpWriter;


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
    dumpWriter = new CommonDumpWriter();
    dumpWriter.addProperty("pathfinder-version", this::getVersion);

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    loadConfig();
    dumpWriter.addProperty("config", () -> {
      try {
        return Files.readString(new File(getDataFolder(), "config.yml").toPath());
      } catch (Throwable t) {
        return t.toString();
      }
    });

    storage = new StorageImpl(nodeTypeRegistry);
    if (!configuration.getDatabase().isCaching()) {
      storage.setCache(CacheLayerImpl.empty());
    }
    StorageUtil.storage = storage;

    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();
    dumpWriter.addProperty("extensions", () -> extensionRegistry.getExtensions().stream()
        .map(Keyed::getKey).map(NamespacedKey::toString).toList());

    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {

    audiences = provideAudiences();

    saveResource("lang/styles.properties", false);

    // Data
    Messages.applyObjectResolvers(TinyTranslations.NM.getObjectResolver());
    translations.setMessageStorage(new PropertiesMessageStorage(new File(getDataFolder(), "/lang/")));
    translations.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "/lang/styles.properties")));
    translations.addMessages(TinyTranslations.messageFieldsFromClass(Messages.class));
    reloadLocales(configuration);

    if (configFileLoader.isVersionChange()) {
      File data = new File(getDataFolder(), "data/");
      File oldData = new File(getDataFolder(), "old_data/");
      if (!FileUtils.renameTo(data, oldData)) {
        shutdownExceptionally(new IllegalStateException("Could not store current data directory to old data."));
        return;
      }
    }

    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = switch (configuration.database.type) {
      case REMOTE_SQL -> new RemoteSqlStorage(configuration.database.remoteSql, nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
      default -> new SqliteStorage(configuration.database.embeddedSql.file, nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
    };
    // impl = new DebugStorage(impl, getLogger());
    impl.setWorldLoader(this::getWorld);
    impl.setLogger(getLogger());

    storage.setImplementation(impl);
    storage.setEventDispatcher(eventDispatcher);
    storage.setLogger(getLogger());
    storage.init();
    setupVisualizerTypes();
    getStorage().createGlobalNodeGroup(visualizerTypeRegistry.getDefaultType()).join();

    nodeTypeRegistry.register(new WaypointType(new WaypointStorage(storage)));

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

    dumpWriter.addProperty("node-types", () -> nodeTypeRegistry.getTypes().stream()
            .map(Keyed::getKey).map(Objects::toString).toList());
    dumpWriter.addProperty("modifier-types", () -> modifierRegistry.getTypes().stream()
            .map(Keyed::getKey).map(Objects::toString).toList());
    dumpWriter.addProperty("visualizer-types", () -> visualizerTypeRegistry.getTypes().keySet().stream()
            .map(Objects::toString).toList());
    dumpWriter.addProperty("node-count", () -> storage.loadNodes().join().size());
    dumpWriter.addProperty("group-count", () -> storage.loadAllGroups().join().size());
    dumpWriter.addProperty("visualizer-count", () -> storage.loadVisualizers().join().size());
  }

  abstract void setupVisualizerTypes();

  @SneakyThrows
  @Override
  public void shutdown() {
    if (state.equals(ApplicationState.DISABLED) || state.equals(ApplicationState.EXCEPTIONALLY)) {
      throw new IllegalStateException("Trying to shutdown PathFinder - Application already disabled.");
    }
    state = ApplicationState.DISABLED;

    NodeHandler.getInstance().close();
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

  @Override
  public void reloadLocales(PathFinderConfig configuration) {
    translations.saveLocale(Locale.ENGLISH);
    translations.loadLocales();
    translations.loadStyles();
    translations.setUseClientLocale(configuration.getLanguage().isClientLanguage());
    translations.setDefaultLocale(configuration.getLanguage().getFallbackLanguage());
  }

  abstract AudienceProvider provideAudiences();

  abstract EventDispatcher<?> provideEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
