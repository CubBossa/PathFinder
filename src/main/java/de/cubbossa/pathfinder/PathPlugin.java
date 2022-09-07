package de.cubbossa.pathfinder;

import de.bossascrew.splinelib.SplineLib;
import de.bossascrew.splinelib.util.BezierVector;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.pathfinder.core.commands.NodeGroupCommand;
import de.cubbossa.pathfinder.core.commands.PathFinderCommand;
import de.cubbossa.pathfinder.core.commands.RoadMapCommand;
import de.cubbossa.pathfinder.core.commands.WaypointCommand;
import de.cubbossa.pathfinder.core.configuration.Configuration;
import de.cubbossa.pathfinder.core.listener.DatabaseListener;
import de.cubbossa.pathfinder.core.listener.PlayerListener;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.data.DataStorage;
import de.cubbossa.pathfinder.data.InMemoryDatabase;
import de.cubbossa.pathfinder.data.SqliteDatabase;
import de.cubbossa.pathfinder.data.YmlDatabase;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.maze.MazeCommand;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.command.CancelPathCommand;
import de.cubbossa.pathfinder.module.visualizing.command.FindCommand;
import de.cubbossa.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.PacketTranslationHandler;
import de.cubbossa.translations.TranslationHandler;
import de.tr7zw.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.nms.NMS_1_19_1_R1;
import dev.jorel.commandapi.test.MockNMS;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PathPlugin extends JavaPlugin {

	public static final String PERM_CMD_PF = "pathfinder.command.pathfinder";
	public static final String PERM_CMD_PF_RELOAD = "pathfinder.command.pathfinder.reload";
	public static final String PERM_CMD_FIND = "pathfinder.command.find";
	public static final String PERM_CMD_FIND_LOC = "pathfinder.command.find.location";
	public static final String PERM_CMD_CANCELPATH = "pathfinder.command.cancel_path";
	public static final String PERM_CMD_RM = "pathfinder.command.roadmap";
	public static final String PERM_CMD_RM_INFO = "pathfinder.command.roadmap.info";
	public static final String PERM_CMD_RM_CREATE = "pathfinder.command.roadmap.create";
	public static final String PERM_CMD_RM_DELETE = "pathfinder.command.roadmap.delete";
	public static final String PERM_CMD_RM_EDITMODE = "pathfinder.command.roadmap.editmode";
	public static final String PERM_CMD_RM_LIST = "pathfinder.command.roadmap.list";
	public static final String PERM_CMD_RM_FORCEFIND = "pathfinder.command.roadmap.forcefind";
	public static final String PERM_CMD_RM_FORCEFORGET = "pathfinder.command.roadmap.forceforget";
	public static final String PERM_CMD_RM_SET_VIS = "pathfinder.command.roadmap.set.path-visualizer";
	public static final String PERM_CMD_RM_SET_NAME = "pathfinder.command.roadmap.set.name";
	public static final String PERM_CMD_RM_SET_WORLD = "pathfinder.command.roadmap.set.world";
	public static final String PERM_CMD_RM_SET_FIND_DIST = "pathfinder.command.roadmap.set.find-distance";
	public static final String PERM_CMD_RM_SET_FINDABLE = "pathfinder.command.roadmap.set.findable";
	public static final String PERM_CMD_RM_SET_CURVE = "pathfinder.command.roadmap.set.curvelength";
	public static final String PERM_CMD_NG = "pathfinder.command.nodegroup";
	public static final String PERM_CMD_NG_LIST = "pathfinder.command.nodegroup.list";
	public static final String PERM_CMD_NG_CREATE = "pathfinder.command.nodegroup.create";
	public static final String PERM_CMD_NG_DELETE = "pathfinder.command.nodegroup.delete";
	public static final String PERM_CMD_NG_SET_NAME = "pathfinder.command.nodegroup.set_name";
	public static final String PERM_CMD_NG_SET_PERM = "pathfinder.command.nodegroup.set_permission";
	public static final String PERM_CMD_NG_SET_NAVIGABLE = "pathfinder.command.nodegroup.set_navigable";
	public static final String PERM_CMD_NG_SET_DISCOVERABLE = "pathfinder.command.nodegroup.set_discoverable";
	public static final String PERM_CMD_NG_SET_DISCOVER_DIST = "pathfinder.command.nodegroup.set_find_distance";
	public static final String PERM_CMD_NG_ST_LIST = "pathfinder.command.nodegroup.searchterms.list";
	public static final String PERM_CMD_NG_ST_ADD = "pathfinder.command.nodegroup.searchterms.add";
	public static final String PERM_CMD_NG_ST_REMOVE = "pathfinder.command.nodegroup.searchterms.remove";
	public static final String PERM_CMD_WP = "pathfinder.command.waypoint";
	public static final String PERM_CMD_WP_INFO = "pathfinder.command.waypoint.info";
	public static final String PERM_CMD_WP_LIST = "pathfinder.command.waypoint.list";
	public static final String PERM_CMD_WP_CREATE = "pathfinder.command.waypoint.create";
	public static final String PERM_CMD_WP_DELETE = "pathfinder.command.waypoint.delete";
	public static final String PERM_CMD_WP_TP = "pathfinder.command.waypoint.tp";
	public static final String PERM_CMD_WP_TPHERE = "pathfinder.command.waypoint.tphere";
	public static final String PERM_CMD_WP_CONNECT = "pathfinder.command.waypoint.connect";
	public static final String PERM_CMD_WP_DISCONNECT = "pathfinder.command.waypoint.disconnect";
	public static final String PERM_CMD_WP_SET_PERM = "pathfinder.command.waypoint.set_perm";
	public static final String PERM_CMD_WP_SET_CURVE = "pathfinder.command.waypoint.set_curve_length";
	public static final String PERM_CMD_PV = "pathfinder.command.visualizer";
	public static final String PERM_CMD_PV_LIST = "pathfinder.command.visualizer.list";
	public static final String PERM_CMD_PV_CREATE = "pathfinder.command.visualizer.create";
	public static final String PERM_CMD_PV_DELETE = "pathfinder.command.visualizer.delete";
	public static final String PERM_CMD_PV_INFO = "pathfinder.command.visualizer.info";
	public static final String PERM_CMD_PV_SET_NAME = "pathfinder.command.visualizer.set_name";
	public static final String PERM_CMD_PV_SET_PERMISSION = "pathfinder.command.visualizer.set_permission";
	public static final String PERM_CMD_PV_INTERVAL = "pathfinder.command.visualizer.set_interval";
	public static final String PERM_CMD_PV_POINT_DIST = "pathfinder.command.visualizer.set_distance";
	public static final String PERM_CMD_PV_SAMPLE_RATE = "pathfinder.command.visualizer.set_sample_rate";
	public static final String PERM_CMD_PV_PARTICLE_STEPS = "pathfinder.command.visualizer.particle.set_particle_steps";
	public static final String PERM_CMD_PV_PARTICLES = "pathfinder.command.visualizer.particle.set_particle";


	public static final SplineLib<Vector> SPLINES = new SplineLib<>() {
		@Override
		public de.bossascrew.splinelib.util.Vector convertToVector(org.bukkit.util.Vector vector) {
			return new de.bossascrew.splinelib.util.Vector(vector.getX(), vector.getY(), vector.getZ());
		}

		@Override
		public org.bukkit.util.Vector convertFromVector(de.bossascrew.splinelib.util.Vector vector) {
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


	public PathPlugin() {
		super();
		instance = this;
		extensions = new ArrayList<>();
	}

	protected PathPlugin(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
		instance = this;
		extensions = new ArrayList<>();
	}

	@SneakyThrows
	@Override
	public void onLoad() {

		generateIfAbsent("config.yml");
		generateIfAbsent("how-the-hell-do-i-use-it.txt");
		generateIfAbsent("lang/de_DE.yml");

		configuration = Configuration.loadFromFile(new File(getDataFolder(), "config.yml"));

		CommandAPI.onLoad(new CommandAPIConfig()
				.verboseOutput(configuration.isVerbose())
				.useLatestNMSVersion(true)
				.initializeNBTAPI(NBTContainer.class, NBTContainer::new));
	}

	@SneakyThrows
	@Override
	public void onEnable() {
		effectsFile = new File(getDataFolder(), "effects.nbo");

		audiences = BukkitAudiences.create(this);
		miniMessage = MiniMessage.miniMessage();

		// Data

		TranslationHandler translationHandler = new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"));
		new PacketTranslationHandler(this);
		translationHandler.registerAnnotatedLanguageClass(Messages.class);
		translationHandler.setFallbackLanguage(configuration.getFallbackLanguage());
		translationHandler.setUseClientLanguage(configuration.isClientLanguage());
		translationHandler.loadLanguages();

		new File(getDataFolder(), "data/").mkdirs();
		database = switch (configuration.getDatabaseType()) {
			case IN_MEMORY -> new InMemoryDatabase(this.getLogger());
			case SQLITE -> new SqliteDatabase(new File(getDataFolder() + "/data/", "database.db"));
			default -> new YmlDatabase(new File(getDataFolder(), "data/"));
		};
		database.connect();

		new FindModule(this);

		if (!effectsFile.exists()) {
			saveResource("effects.nbo", false);
		}
		new EffectHandler(this, TranslationHandler.getInstance().getAudiences(), TranslationHandler.getInstance().getMiniMessage(),
				context -> TranslationHandler.getInstance().translateLine(context.text(), context.player(), context.resolver()));

		new NodeGroupHandler().loadGroups();
		new VisualizerHandler();
		new NodeTypeHandler();
		new RoadMapHandler().loadRoadMaps();
		new DiscoverHandler();
		new GUIHandler(this).enable();

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
		Bukkit.getPluginManager().registerEvents(new DatabaseListener(database), this);

		extensions.forEach(PathPluginExtension::onEnable);

		Metrics metrics = new Metrics(this, 16324);
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
		RoadMapHandler.getInstance().cancelAllEditModes();
		GUIHandler.getInstance().disable();
	}

	public void registerExtension(PathPluginExtension module) {
		extensions.add(module);
	}

	private void generateIfAbsent(String resource) {
		if (!new File(getDataFolder(), resource).exists()) {
			saveResource(resource, false);
		}
	}
}
