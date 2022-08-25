package de.bossascrew.pathfinder;

import de.bossascrew.pathfinder.core.commands.*;
import de.bossascrew.pathfinder.core.configuration.Configuration;
import de.bossascrew.pathfinder.core.listener.DatabaseListener;
import de.bossascrew.pathfinder.core.listener.PlayerListener;
import de.bossascrew.pathfinder.core.node.NodeGroupHandler;
import de.bossascrew.pathfinder.core.node.NodeTypeHandler;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.data.*;
import de.bossascrew.pathfinder.module.Module;
import de.bossascrew.pathfinder.module.discovering.DiscoverHandler;
import de.bossascrew.pathfinder.module.maze.MazeCommand;
import de.bossascrew.pathfinder.module.visualizing.FindModule;
import de.bossascrew.pathfinder.module.visualizing.VisualizerHandler;
import de.bossascrew.pathfinder.module.visualizing.command.FindCommand;
import de.bossascrew.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.bossascrew.splinelib.SplineLib;
import de.bossascrew.splinelib.util.BezierVector;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.serializedeffects.EffectHandler;
import de.cubbossa.translations.PacketTranslationHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathPlugin extends JavaPlugin {

	public static final String PERM_FIND_NODE = "bcrew.pathfinder.find";
	public static final String PERM_COMMAND_FIND_INFO = "pathfinder.command.find.info";
	public static final String PERM_COMMAND_FIND_STYLE = "pathfinder.command.find.style";
	public static final String PERM_COMMAND_FIND_LOCATIONS = "pathfinder.command.find.location";

	public static final String PERM_CMD_NG_LIST = "pathfinder.nodegroup.list";
	public static final String PERM_CMD_NG_CREATE = "pathfinder.nodegroup.create";
	public static final String PERM_CMD_NG_DELETE = "pathfinder.nodegroup.delete";
	public static final String PERM_CMD_NG_RENAME = "pathfinder.nodegroup.rename";
	public static final String PERM_CMD_NG_SET_FINDABLE = "pathfinder.nodegroup.set_findable";
	public static final String PERM_CMD_NG_ST_LIST = "pathfinder.nodegroup.searchterms.list";
	public static final String PERM_CMD_NG_ST_ADD = "pathfinder.nodegroup.searchterms.add";
	public static final String PERM_CMD_NG_ST_REMOVE = "pathfinder.nodegroup.searchterms.remove";
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

	private final List<Module> modules;

	@Getter
	private BukkitAudiences audiences;
	@Getter
	private MiniMessage miniMessage;
	@Getter
	private DataStorage database;
	@Getter
	private Configuration configuration;

	public PathPlugin() {
		instance = this;
		modules = new ArrayList<>();
	}


	@Override
	public void onLoad() {
		CommandAPI.onLoad(new CommandAPIConfig()
				.verboseOutput(true));
	}

	@SneakyThrows
	@Override
	public void onEnable() {
		audiences = BukkitAudiences.create(this);
		miniMessage = MiniMessage.miniMessage();

		configuration = Configuration.loadFromFile(new File(getDataFolder(), "config.yml"));

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

		System.out.println(database);

		new FindModule(this);

		//new EffectHandler(this, TranslationHandler.getInstance().getAudiences(), TranslationHandler.getInstance().getMiniMessage());

		new NodeGroupHandler().loadGroups();
		new VisualizerHandler();
		new NodeTypeHandler();
		new PathPlayerHandler();
		new RoadMapHandler().loadRoadMaps();
		new DiscoverHandler();
		new GUIHandler(this).enable();

		// Commands

		CommandAPI.onEnable(this);
		new FindCommand().register();
		new RoadMapCommand().register();
		new PathFinderCommand().register();
		new CancelPathCommand().register();
		new NodeGroupCommand(0).register();
		new PathVisualizerCommand(0).register();
		new WaypointCommand().register();
		new MazeCommand().register();

		// Listeners

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new DatabaseListener(database), this);

		modules.forEach(Module::onEnable);
	}

	@SneakyThrows
	@Override
	public void onDisable() {

		CommandAPI.unregister("nodegroup");
		CommandAPI.unregister("roadmap");
		RoadMapHandler.getInstance().cancelAllEditModes();
		GUIHandler.getInstance().disable();

	}

	public void registerModule(Module module) {
		modules.add(module);
	}
}
