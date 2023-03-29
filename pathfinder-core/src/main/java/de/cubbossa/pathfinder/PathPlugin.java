package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.ExamplesHandler;
import de.cubbossa.pathfinder.core.listener.PlayerListener;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.core.node.WaypointType;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.RemoteSqlDataStorage;
import de.cubbossa.pathfinder.data.SqliteDataStorage;
import de.cubbossa.pathfinder.data.YmlDataStorage;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.splinelib.util.BezierVector;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

@Getter
public class PathPlugin extends JavaPlugin {

  public static final SplineLib<Vector> SPLINES = new SplineLib<>() {
    @Override
    public de.cubbossa.splinelib.util.Vector convertToVector(org.bukkit.util.Vector vector) {
      return new de.cubbossa.splinelib.util.Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public org.bukkit.util.Vector convertFromVector(de.cubbossa.splinelib.util.Vector vector) {
      return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public BezierVector convertToBezierVector(org.bukkit.util.Vector vector) {
      return new BezierVector(vector.getX(), vector.getY(), vector.getZ(), null, null);
    }

    @Override
    public org.bukkit.util.Vector convertFromBezierVector(BezierVector bezierVector) {
      return new Vector(bezierVector.getX(), bezierVector.getY(), bezierVector.getZ());
    }
  };

  @Getter
  private static PathPlugin instance;

  private BukkitAudiences audiences;
  private MiniMessage miniMessage;

  private File effectsFile;
  private final NodeTypeRegistry nodeTypeRegistry;
  private DataStorage database;
  @Setter
  private PathPluginConfig configuration;
  private final ExtensionsRegistry extensionsRegistry;
  private final CommandRegistry commandRegistry;
  private final BStatsLoader bStatsLoader;
  private final ConfigFileLoader configFileLoader;

  private NodeType<Waypoint> waypointNodeType;

  public PathPlugin() {
    instance = this;

    nodeTypeRegistry = new NodeTypeRegistry();
    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    bStatsLoader = new BStatsLoader();
    commandRegistry = new CommandRegistry();
    extensionsRegistry = new ExtensionsRegistry();
    extensionsRegistry.findServiceExtensions(this.getClassLoader());
  }

  @SneakyThrows
  @Override
  public void onLoad() {

    generateIfAbsent("lang/styles.yml");
    generateIfAbsent("lang/de_DE.yml");

    YamlUtils.registerClasses();
    loadConfig();

    commandRegistry.loadCommands();
    extensionsRegistry.loadExtensions(this);
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
    database = switch (configuration.database.type) {
      case IN_MEMORY -> null;
      case SQLITE -> new SqliteDataStorage(configuration.database.embeddedSql.file, nodeTypeRegistry);
      case REMOTE_SQL -> new RemoteSqlDataStorage(configuration.database.remoteSql, nodeTypeRegistry);
      default -> new YmlDataStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry);
    };
    if (database != null) {
      database.connect(() -> {
        ExamplesHandler examples = ExamplesHandler.getInstance();
        examples.afterFetch(() -> {
          examples.getExamples().forEach(examples::loadVisualizer);
        });
      });
    }

    setWaypointNodeType(new WaypointType(
        database,
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
    new NodeHandler();
    new DiscoverHandler(this);

    VisualizerHandler.getInstance().loadVisualizers();

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

    commandRegistry.enableCommands(this);
    extensionsRegistry.enableExtensions(this);
  }

  @SneakyThrows
  @Override
  public void onDisable() {
    NodeHandler.getInstance().cancelAllEditModes();

    extensionsRegistry.disableExtensions(this);
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

  public void setWaypointNodeType(NodeType<Waypoint> nodeType) {
    waypointNodeType = nodeType;
    nodeTypeRegistry.setWaypointNodeType(nodeType);
    nodeTypeRegistry.registerNodeType(nodeType);
  }
}
