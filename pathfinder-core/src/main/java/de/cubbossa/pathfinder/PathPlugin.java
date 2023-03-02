package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.ExamplesHandler;
import de.cubbossa.pathfinder.core.commands.NodeGroupCommand;
import de.cubbossa.pathfinder.core.commands.PathFinderCommand;
import de.cubbossa.pathfinder.core.commands.RoadMapCommand;
import de.cubbossa.pathfinder.core.commands.WaypointCommand;
import de.cubbossa.pathfinder.core.configuration.Configuration;
import de.cubbossa.pathfinder.core.listener.DatabaseListener;
import de.cubbossa.pathfinder.core.listener.PlayerListener;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.SqliteDatabase;
import de.cubbossa.pathfinder.data.YmlDatabase;
import de.cubbossa.pathfinder.hook.PlaceholderHookLoader;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.maze.MazeCommand;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.command.CancelPathCommand;
import de.cubbossa.pathfinder.module.visualizing.command.FindCommand;
import de.cubbossa.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.splinelib.util.BezierVector;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

@Getter
public class PathPlugin extends JavaPlugin {

  public static final String PERM_CMD_PF_INFO = "pathfinder.command.pathfinder.info";
  public static final String PERM_CMD_PF_HELP = "pathfinder.command.pathfinder.help";
  public static final String PERM_CMD_PF_RELOAD = "pathfinder.command.pathfinder.reload";
  public static final String PERM_CMD_PF_EXPORT = "pathfinder.command.pathfinder.export";
  public static final String PERM_CMD_PF_IMPORT = "pathfinder.command.pathfinder.import";
  public static final String PERM_CMD_FIND = "pathfinder.command.find";
  public static final String PERM_CMD_CANCELPATH = "pathfinder.command.cancel_path";
  public static final String PERM_CMD_RM_INFO = "pathfinder.command.roadmap.info";
  public static final String PERM_CMD_RM_CREATE = "pathfinder.command.roadmap.create";
  public static final String PERM_CMD_RM_DELETE = "pathfinder.command.roadmap.delete";
  public static final String PERM_CMD_RM_EDITMODE = "pathfinder.command.roadmap.editmode";
  public static final String PERM_CMD_RM_LIST = "pathfinder.command.roadmap.list";
  public static final String PERM_CMD_RM_FORCEFIND = "pathfinder.command.roadmap.forcefind";
  public static final String PERM_CMD_RM_FORCEFORGET = "pathfinder.command.roadmap.forceforget";
  public static final String PERM_CMD_RM_SET_VIS = "pathfinder.command.roadmap.set_visualizer";
  public static final String PERM_CMD_RM_SET_NAME = "pathfinder.command.roadmap.set_name";
  public static final String PERM_CMD_RM_SET_CURVE = "pathfinder.command.roadmap.set_curvelength";
  public static final String PERM_CMD_NG_LIST = "pathfinder.command.nodegroup.list";
  public static final String PERM_CMD_NG_CREATE = "pathfinder.command.nodegroup.create";
  public static final String PERM_CMD_NG_DELETE = "pathfinder.command.nodegroup.delete";
  public static final String PERM_CMD_NG_SET_NAME = "pathfinder.command.nodegroup.set_name";
  public static final String PERM_CMD_NG_SET_PERM = "pathfinder.command.nodegroup.set_permission";
  public static final String PERM_CMD_NG_SET_NAVIGABLE =
      "pathfinder.command.nodegroup.set_navigable";
  public static final String PERM_CMD_NG_SET_DISCOVERABLE =
      "pathfinder.command.nodegroup.set_discoverable";
  public static final String PERM_CMD_NG_SET_DISCOVER_DIST =
      "pathfinder.command.nodegroup.set_find_distance";
  public static final String PERM_CMD_NG_ST_LIST = "pathfinder.command.nodegroup.searchterms.list";
  public static final String PERM_CMD_NG_ST_ADD = "pathfinder.command.nodegroup.searchterms.add";
  public static final String PERM_CMD_NG_ST_REMOVE =
      "pathfinder.command.nodegroup.searchterms.remove";
  public static final String PERM_CMD_WP_INFO = "pathfinder.command.waypoint.info";
  public static final String PERM_CMD_WP_LIST = "pathfinder.command.waypoint.list";
  public static final String PERM_CMD_WP_CREATE = "pathfinder.command.waypoint.create";
  public static final String PERM_CMD_WP_DELETE = "pathfinder.command.waypoint.delete";
  public static final String PERM_CMD_WP_TP = "pathfinder.command.waypoint.tp";
  public static final String PERM_CMD_WP_TPHERE = "pathfinder.command.waypoint.tphere";
  public static final String PERM_CMD_WP_CONNECT = "pathfinder.command.waypoint.connect";
  public static final String PERM_CMD_WP_DISCONNECT = "pathfinder.command.waypoint.disconnect";
  public static final String PERM_CMD_WP_SET_CURVE = "pathfinder.command.waypoint.set_curve_length";
  public static final String PERM_CMD_WP_ADD_GROUP = "pathfinder.command.waypoint.add_group";
  public static final String PERM_CMD_WP_REMOVE_GROUP = "pathfinder.command.waypoint.remove_group";
  public static final String PERM_CMD_WP_CLEAR_GROUPS = "pathfinder.command.waypoint.clear_groups";
  public static final String PERM_CMD_PV_LIST = "pathfinder.command.visualizer.list";
  public static final String PERM_CMD_PV_CREATE = "pathfinder.command.visualizer.create";
  public static final String PERM_CMD_PV_DELETE = "pathfinder.command.visualizer.delete";
  public static final String PERM_CMD_PV_INFO = "pathfinder.command.visualizer.info";
  public static final String PERM_CMD_PV_SET_NAME = "pathfinder.command.visualizer.set_name";
  public static final String PERM_CMD_PV_SET_PERMISSION =
      "pathfinder.command.visualizer.set_permission";
  public static final String PERM_CMD_PV_INTERVAL = "pathfinder.command.visualizer.set_interval";
  public static final String PERM_CMD_PV_EDIT = "pathfinder.command.visualizer.edit";

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

  private final List<PathPluginExtension> extensions;
  private BukkitAudiences audiences;
  private MiniMessage miniMessage;

  private File effectsFile;
  private DataStorage database;
  @Setter
  private Configuration configuration;

  private FindCommand findCommand;
  private RoadMapCommand roadMapCommand;
  private PathFinderCommand pathFinderCommand;
  private CancelPathCommand cancelPathCommand;
  private NodeGroupCommand nodeGroupCommand;
  private PathVisualizerCommand pathVisualizerCommand;
  private WaypointCommand waypointCommand;
  private MazeCommand mazeCommand;

  private final Set<DependencyLoader> dependencies;

  public PathPlugin() {
    super();
    instance = this;
    extensions = new ArrayList<>();
    dependencies = Set.of(
        new DependencyLoader("PlaceholderAPI", PlaceholderHookLoader::load, false)
    );

    ServiceLoader<PathPluginExtension> loader = ServiceLoader.load(PathPluginExtension.class, this.getClassLoader());
    loader.forEach(extensions::add);
  }

  @SneakyThrows
  @Override
  public void onLoad() {

    generateIfAbsent("lang/styles.yml");
    generateIfAbsent("lang/de_DE.yml");

    configuration = Configuration.loadFromFile(new File(getDataFolder(), "config.yml"));

    YamlUtils.registerClasses();

    CommandAPI.onLoad(new CommandAPIConfig()
        .verboseOutput(configuration.isVerbose()));

    extensions.forEach(PathPluginExtension::onLoad);
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
    translationHandler.setFallbackLanguage(configuration.getFallbackLanguage());
    translationHandler.setUseClientLanguage(configuration.isClientLanguage());
    translationHandler.loadStyle();
    translationHandler.loadLanguages();

    new File(getDataFolder(), "data/").mkdirs();
    database = switch (configuration.getDatabaseType()) {
      case IN_MEMORY -> null;
      case SQLITE -> new SqliteDatabase(new File(getDataFolder() + "/data/", "database.db"));
      default -> new YmlDatabase(new File(getDataFolder(), "data/"));
    };
    if (database != null) {
      database.connect(() -> {
        ExamplesHandler examples = ExamplesHandler.getInstance();
        examples.afterFetch(() -> {
          examples.getExamples().forEach(examples::loadVisualizer);
        });
      });
    }

    new FindModule(this);

    if (!effectsFile.exists()) {
      saveResource("effects.nbo", false);
    }
    new EffectHandler(this, TranslationHandler.getInstance().getAudiences(),
        TranslationHandler.getInstance().getMiniMessage(),
        context -> TranslationHandler.getInstance()
            .translateLine(context.text(), context.player(), context.resolver()));

    new NodeGroupHandler();
    new VisualizerHandler();
    new NodeTypeHandler();
    new RoadMapHandler();
    new DiscoverHandler();

    dependencies.forEach(DependencyLoader::enable);

    NodeGroupHandler.getInstance().loadGroups();
    VisualizerHandler.getInstance().loadVisualizers();
    RoadMapHandler.getInstance().loadRoadMaps();

    // Commands

    CommandAPI.onEnable(this);
    findCommand = new FindCommand();
    findCommand.register();
    roadMapCommand = new RoadMapCommand();
    roadMapCommand.register();
    pathFinderCommand = new PathFinderCommand();
    pathFinderCommand.register();
    cancelPathCommand = new CancelPathCommand();
    cancelPathCommand.register();
    nodeGroupCommand = new NodeGroupCommand(0);
    nodeGroupCommand.register();
    pathVisualizerCommand = new PathVisualizerCommand();
    pathVisualizerCommand.register();
    waypointCommand = new WaypointCommand();
    waypointCommand.register();
    if (configuration.isTesting()) {
      mazeCommand = new MazeCommand();
      mazeCommand.register();
    }

    // Listeners

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    if (database != null) {
      Bukkit.getPluginManager().registerEvents(new DatabaseListener(database), this);
    }

    extensions.forEach(PathPluginExtension::onEnable);

    Metrics metrics = new Metrics(this, 16324);

    metrics.addCustomChart(new SimplePie("roadmap_amount",
        () -> RoadMapHandler.getInstance().getRoadMaps().size() + ""));
    metrics.addCustomChart(new SimplePie("group_amount",
        () -> NodeGroupHandler.getInstance().getNodeGroups().size() + ""));
    metrics.addCustomChart(new SimplePie("visualizer_amount",
        () -> VisualizerHandler.getInstance().getRoadmapVisualizers().size() + ""));
    metrics.addCustomChart(new AdvancedPie("nodes_per_roadmap", () -> {
      IntStream counts = RoadMapHandler.getInstance().getRoadMapsStream()
          .map(RoadMap::getNodes)
          .mapToInt(Collection::size);
      Map<String, Integer> vals = new HashMap<>();
      counts.forEach(value -> {
        if (value < 10) {
          vals.put("< 10", vals.getOrDefault("< 10", 0) + 1);
        } else if (value < 30) {
          vals.put("10-30", vals.getOrDefault("10-30", 0) + 1);
        } else if (value < 50) {
          vals.put("30-50", vals.getOrDefault("30-50", 0) + 1);
        } else if (value < 100) {
          vals.put("50-100", vals.getOrDefault("50-100", 0) + 1);
        } else if (value < 150) {
          vals.put("100-150", vals.getOrDefault("100-150", 0) + 1);
        } else if (value < 200) {
          vals.put("150-200", vals.getOrDefault("150-200", 0) + 1);
        } else if (value < 300) {
          vals.put("200-300", vals.getOrDefault("200-300", 0) + 1);
        } else if (value < 500) {
          vals.put("300-500", vals.getOrDefault("300-500", 0) + 1);
        } else {
          vals.put("> 500", vals.getOrDefault("> 500", 0) + 1);
        }
      });
      return vals;
    }));
  }

  @SneakyThrows
  @Override
  public void onDisable() {

    CommandAPI.unregister(findCommand.getName());
    CommandAPI.unregister(roadMapCommand.getName());
    CommandAPI.unregister(pathFinderCommand.getName());
    CommandAPI.unregister(cancelPathCommand.getName());
    CommandAPI.unregister(nodeGroupCommand.getName());
    CommandAPI.unregister(pathVisualizerCommand.getName());
    CommandAPI.unregister(waypointCommand.getName());
    if (configuration.isTesting()) {
      CommandAPI.unregister(mazeCommand.getName());
    }
    CommandAPI.onDisable();

    RoadMapHandler.getInstance().cancelAllEditModes();

    extensions.forEach(PathPluginExtension::onDisable);
  }

  public void registerExtension(PathPluginExtension module) {
    extensions.add(module);
  }

  public void generateIfAbsent(String resource) {
    if (!new File(getDataFolder(), resource).exists()) {
      saveResource(resource, false);
    }
  }
}
