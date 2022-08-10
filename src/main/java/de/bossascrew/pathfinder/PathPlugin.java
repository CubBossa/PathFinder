package de.bossascrew.pathfinder;

import co.aikar.commands.*;
import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.core.commands.CancelPathCommand;
import de.bossascrew.pathfinder.core.commands.PathFinderCommand;
import de.bossascrew.pathfinder.core.commands.RoadMapCommand;
import de.bossascrew.pathfinder.core.configuration.Configuration;
import de.bossascrew.pathfinder.core.listener.DatabaseListener;
import de.bossascrew.pathfinder.core.listener.PlayerListener;
import de.bossascrew.pathfinder.core.node.Navigable;
import de.bossascrew.pathfinder.core.node.NavigateSelection;
import de.bossascrew.pathfinder.core.node.NodeGroup;
import de.bossascrew.pathfinder.core.node.NodeTypeHandler;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.data.*;
import de.bossascrew.pathfinder.module.Module;
import de.bossascrew.pathfinder.module.visualizing.FindModule;
import de.bossascrew.pathfinder.module.visualizing.VisualizerHandler;
import de.bossascrew.pathfinder.module.visualizing.command.FindCommand;
import de.bossascrew.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.bossascrew.pathfinder.util.SetArithmeticParser;
import de.bossascrew.splinelib.SplineLib;
import de.bossascrew.splinelib.util.BezierVector;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.translations.PacketTranslationHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static final String COMPLETE_ROADMAPS = "@roadmaps";
	public static final String COMPLETE_ACTIVE_ROADMAPS = "@activeroadmaps";
	public static final String COMPLETE_PATH_VISUALIZER = "@path_visualizer";
	public static final String COMPLETE_PATH_VISUALIZER_STYLES = "@path_visualizer_styles";
	public static final String COMPLETE_EDITMODE_VISUALIZER = "@editmode_visualizer";
	public static final String COMPLETE_NODE_SELECTION = "@nodes";
	public static final String COMPLETE_NAVIGABLES = "@navigables";
	public static final String COMPLETE_FINDABLES_CONNECTED = "@nodes_connected";
	public static final String COMPLETE_FINDABLES_FINDABLE = "@nodes_findable";
	public static final String COMPLETE_FINDABLES_FOUND = "@nodes_found";
	public static final String COMPLETE_GROUPS_BY_PARAMETER = "@nodegroups_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_PARAMETER = "@nodegroups_findable_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_SELECTION = "@nodegroups_findable_selection";
	public static final String COMPLETE_FINDABLE_LOCATIONS = "@findable_locations";

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

		// Commands

		CommandAPI.onEnable(this);
		//new ArgumentTree(new NodeGroupCommand()).register();
		new FindCommand().register();
		new RoadMapCommand().register();
		new PathFinderCommand().register();
		new CancelPathCommand().register();
		new PathVisualizerCommand(0).register();

		new FindModule(this);

		new VisualizerHandler();
		new NodeTypeHandler();
		new PathPlayerHandler();
		new RoadMapHandler().loadRoadMaps();

		new GUIHandler(this).enable();

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
