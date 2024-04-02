package de.cubbossa.pathfinder;

import de.cubbossa.disposables.Disposer;
import de.cubbossa.pathapi.ExtensionsRegistry;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.dump.DumpWriter;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageAdapter;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.dump.DumpWriterImpl;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.migration.Migrator;
import de.cubbossa.pathfinder.node.GraphEditorRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.StorageAdapterImpl;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.TinyTranslations;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesStyleStorage;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AbstractPathFinder implements PathFinder {


  @Getter
  private static AbstractPathFinder instance;

  private static final NamespacedKey GLOBAL_GROUP_KEY = pathfinder("global");
  private static final NamespacedKey DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer");
  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  protected Disposer disposer;

  protected ApplicationState state = ApplicationState.DISABLED;

  protected NodeTypeRegistry nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected ExtensionsRegistry extensionRegistry;
  protected ConfigFileLoader configFileLoader;
  protected AudienceProvider audiences;
  protected StorageAdapter storage;
  @Setter
  protected PathFinderConfig configuration;
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

  public AbstractPathFinder() {
    disposer = createDisposer();
  }

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

    dumpWriter = new DumpWriterImpl();
    dumpWriter.addProperty("pathfinder-version", this::getVersion);
    disposer.register(this, dumpWriter);

    nodeTypeRegistry = new NodeTypeRegistryImpl(this);
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl(this);
    modifierRegistry = new ModifierRegistryImpl(this);

    configFileLoader = new ConfigFileLoader(getDataFolder());
    disposer.register(this, configFileLoader);
    configuration = configFileLoader.loadConfig();

    dumpWriter.addProperty("config", () -> {
      try {
        return Files.readString(new File(getDataFolder(), "config.yml").toPath());
      } catch (Throwable t) {
        return t.toString();
      }
    });

    storage = new StorageAdapterImpl(nodeTypeRegistry);
    disposer.register(this, storage);
    if (!getConfiguration().getDatabase().isCaching()) {
      storage.setCache(CacheLayerImpl.empty());
    }
    StorageUtil.storage = storage;

    eventDispatcher = createEventDispatcher();
    disposer.register(this, eventDispatcher);

    this.extensionRegistry = new ExtensionsRegistryImpl(this);
    extensionRegistry.loadExtensions();
    dumpWriter.addProperty("extensions", () -> extensionRegistry.getExtensions().stream()
        .map(e -> e.isDisabled() ? ("~ " + e.getKey()) : e.getKey().toString())
        .toList());
  }

  @SneakyThrows
  public void onEnable() {

    audiences = createAudiences();

    if (!new File(getDataFolder(), "lang/styles.properties").exists()) {
      saveResource("lang/styles.properties", false);
    }

    // Data
    setupMessages();

    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = getStorageImplementation();

    new Migrator(getDataFolder(), getLogger()).migrate();

    ((StorageAdapterImpl) storage).setImplementation(impl);
    storage.setEventDispatcher(eventDispatcher);
    ((StorageAdapterImpl) storage).setLogger(getLogger());
    storage.init();
    storage.createGlobalNodeGroup(visualizerTypeRegistry.getDefaultType()).join();

    disposer.register(this, new GraphEditorRegistry(this));
    extensionRegistry.enableExtensions();

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

  @Override
  public void dispose() {
    instance = null;
    PathFinderProvider.setPathFinder(null);
  }

  @SneakyThrows
  @Override
  public void shutdown() {
    if (state.equals(ApplicationState.DISABLED) || state.equals(ApplicationState.EXCEPTIONALLY)) {
      throw new IllegalStateException("Trying to shutdown PathFinder - Application already disabled.");
    }
    disposer.dispose(this);
    state = ApplicationState.DISABLED;
  }

  @Override
  public void shutdownExceptionally(Throwable t) {
    shutdown();
    state = ApplicationState.EXCEPTIONALLY;
  }

  private void setupMessages() {

    Messages.applyObjectResolvers(translations);
    translations.setMessageStorage(new PropertiesMessageStorage(new File(getDataFolder(), "/lang/")));
    translations.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "/lang/styles.properties")));
    translations.addMessages(TinyTranslations.messageFieldsFromClass(Messages.class));
    reloadLocales(configuration);
  }

  @NotNull
  private StorageImplementation getStorageImplementation() {
    StorageImplementation impl = switch (getConfiguration().getDatabase().getType()) {
      case REMOTE_SQL ->
          new RemoteSqlStorage(configuration.getDatabase().getRemoteSql(), nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
      default ->
          new SqliteStorage(getConfiguration().getDatabase().getEmbeddedSql().getFile(), nodeTypeRegistry,
              modifierRegistry, visualizerTypeRegistry);
    };
    // impl = new DebugStorage(impl, getLogger());
    impl.setWorldLoader(this::getWorld);
    impl.setLogger(getLogger());
    return impl;
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
    translations.defaultLocale(configuration.getLanguage().getFallbackLanguage());
  }

  abstract AudienceProvider createAudiences();

  abstract Disposer createDisposer();

  abstract EventDispatcher<?> createEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
