package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.ExamplesHandler;
import de.cubbossa.pathfinder.core.commands.NodeGroupCommand;
import de.cubbossa.pathfinder.core.commands.PathFinderCommand;
import de.cubbossa.pathfinder.core.commands.WaypointCommand;
import de.cubbossa.pathfinder.core.listener.PlayerListener;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.RemoteSqlDataStorage;
import de.cubbossa.pathfinder.data.SqliteDataStorage;
import de.cubbossa.pathfinder.data.YmlDataStorage;
import de.cubbossa.pathfinder.hook.PlaceholderHookLoader;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.Version;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.splinelib.util.BezierVector;
import de.cubbossa.translations.TranslationHandler;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
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

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

@Getter
public class PathPlugin extends JavaPlugin {

  public static final String PERM_CMD_PF_INFO = "pathfinder.command.pathfinder.info";
  public static final String PERM_CMD_PF_HELP = "pathfinder.command.pathfinder.help";
  public static final String PERM_CMD_PF_RELOAD = "pathfinder.command.pathfinder.reload";
  public static final String PERM_CMD_PF_EXPORT = "pathfinder.command.pathfinder.export";
  public static final String PERM_CMD_PF_IMPORT = "pathfinder.command.pathfinder.import";
  public static final String PERM_CMD_PF_MODULES = "pathfinder.command.pathfinder.modules";
  public static final String PERM_CMD_PF_FORCEFIND = "pathfinder.command.pathfinder.forcefind";
  public static final String PERM_CMD_PF_FORCEFORGET = "pathfinder.command.pathfinder.forceforget";
  public static final String PERM_CMD_FIND = "pathfinder.command.find";
  public static final String PERM_CMD_FIND_LOCATION = "pathfinder.command.findlocation";
  public static final String PERM_CMD_CANCELPATH = "pathfinder.command.cancel_path";
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
  private PathPluginConfig configuration;

  private PathFinderCommand pathFinderCommand;
  private NodeGroupCommand nodeGroupCommand;
  private PathVisualizerCommand pathVisualizerCommand;
  private WaypointCommand waypointCommand;

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

    YamlUtils.registerClasses();
    loadConfig();

    CommandAPI.onLoad(new CommandAPIConfig());

    new ArrayList<>(extensions).forEach(PathPluginExtension::onLoad);
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
      case SQLITE -> new SqliteDataStorage(configuration.database.embeddedSql.file);
      case REMOTE_SQL -> new RemoteSqlDataStorage(configuration.database.remoteSql);
      default -> new YmlDataStorage(new File(getDataFolder(), "data/"));
    };
    if (database != null) {
      database.connect(() -> {
        ExamplesHandler examples = ExamplesHandler.getInstance();
        examples.afterFetch(() -> {
          examples.getExamples().forEach(examples::loadVisualizer);
        });
      });
    }

    if (!effectsFile.exists()) {
      saveResource("effects.nbo", false);
    }
    new EffectHandler(this, TranslationHandler.getInstance().getAudiences(),
        TranslationHandler.getInstance().getMiniMessage(),
        context -> TranslationHandler.getInstance()
            .translateLine(context.text(), context.player(), context.resolver()));

    new VisualizerHandler();
    new NodeHandler();
    new DiscoverHandler();

    dependencies.forEach(DependencyLoader::enable);

    VisualizerHandler.getInstance().loadVisualizers();

    // Commands

    CommandAPI.onEnable(this);
    pathFinderCommand = new PathFinderCommand();
    pathFinderCommand.register();
    nodeGroupCommand = new NodeGroupCommand(0);
    nodeGroupCommand.register();
    pathVisualizerCommand = new PathVisualizerCommand();
    pathVisualizerCommand.register();
    waypointCommand = new WaypointCommand();
    waypointCommand.register();

    // Listeners

    Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

    new ArrayList<>(extensions).forEach(PathPluginExtension::onEnable);

    Metrics metrics = new Metrics(this, 16324);

    metrics.addCustomChart(new SimplePie("group_amount",
        () -> PathFinderAPI.get().getNodeGroupKeySet().join().size() + ""));
    metrics.addCustomChart(new SimplePie("visualizer_amount",
        () -> VisualizerHandler.getInstance().getRoadmapVisualizers().size() + ""));
    metrics.addCustomChart(new AdvancedPie("nodes_per_group", () -> {
      IntStream counts = PathFinderAPI.get().getNodeGroups().join().stream()
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

    CommandUtils.unregister(pathFinderCommand);
    CommandUtils.unregister(nodeGroupCommand);
    CommandUtils.unregister(pathVisualizerCommand);
    CommandUtils.unregister(waypointCommand);

    new ArrayList<>(extensions).forEach(PathPluginExtension::onDisable);

    NodeHandler.getInstance().cancelAllEditModes();
    CommandAPI.onDisable();
  }

  public void registerExtension(PathPluginExtension module) {
    extensions.add(module);
  }

  public void unregisterExtension(PathPluginExtension module) {
    extensions.remove(module);
  }

  public void generateIfAbsent(String resource) {
    if (!new File(getDataFolder(), resource).exists()) {
      saveResource(resource, false);
    }
  }

  public static final Version CONFIG_REGEN_VERSION = new Version("3.0.0");

  public void loadConfig () {
    File configFile = new File(getDataFolder(), "config.yml");
    YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
        .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
        .createParentDirectories(true)
        .header("""
            #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
            #                                                               #
            #       _____      _   _     ______ _           _               #
            #      |  __ \\    | | | |   |  ____(_)         | |              #
            #      | |__) |_ _| |_| |__ | |__   _ _ __   __| | ___ _ __     #
            #      |  ___/ _` | __| '_ \\|  __| | | '_ \\ / _` |/ _ \\ '__|    #
            #      | |  | (_| | |_| | | | |    | | | | | (_| |  __/ |       #
            #      |_|   \\__,_|\\__|_| |_|_|    |_|_| |_|\\__,_|\\___|_|       #
            #                        Configuration                          #
            #                                                               #
            #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
            
            """)
        .build();

    if (!configFile.exists()) {
      configuration = new PathPluginConfig();
      YamlConfigurations.save(configFile.toPath(), PathPluginConfig.class, configuration, properties);
      return;
    }
    configuration = YamlConfigurations.load(configFile.toPath(), PathPluginConfig.class, properties);

    if (new Version(configuration.version).compareTo(CONFIG_REGEN_VERSION) < 0) {

      saveFileAsOld(configFile, "config", ".yml");
      saveFileAsOld(new File(getDataFolder(), "effects.nbo"), "effects", ".nbo");
      loadConfig();
      saveResource("effects.nbo", true);
    }
  }

  private void saveFileAsOld(File file, String base, String suffix) {
    int test = 1;
    String b = base + "_old";
    File f = new File(file.getParentFile(), b + suffix);
    while (f.exists()) {
      b = String.format("%s_old_%02d%s", base, test++, suffix);
      f = new File(file.getParentFile(), b);
    }
    file.renameTo(f);
  }
}
