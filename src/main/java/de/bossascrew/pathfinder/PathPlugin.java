package de.bossascrew.pathfinder;

import co.aikar.commands.*;
import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.commands.*;
import de.bossascrew.pathfinder.configuration.Configuration;
import de.bossascrew.pathfinder.data.*;
import de.bossascrew.pathfinder.listener.DatabaseListener;
import de.bossascrew.pathfinder.listener.PlayerListener;
import de.bossascrew.pathfinder.node.Navigable;
import de.bossascrew.pathfinder.node.NavigateSelection;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.node.NodeTypeHandler;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.bossascrew.pathfinder.util.SetArithmeticParser;
import de.bossascrew.pathfinder.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.visualizer.VisualizerHandler;
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

	@Getter
	private BukkitAudiences audiences;
	@Getter
	private MiniMessage miniMessage;

	private BukkitCommandManager commandManager;
	@Getter
	private DataStorage database;
	@Getter
	private Configuration configuration;


	@Override
	public void onLoad() {
		CommandAPI.onLoad(new CommandAPIConfig()
				.verboseOutput(true));
	}

	@SneakyThrows
	@Override
	public void onEnable() {
		instance = this;

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
		new NodeGroupCommand().register();
		new FindCommand().register();
		new RoadMapCommand().register();
		new PathFinderCommand().register();
		new CancelPathCommand().register();

		commandManager = new BukkitCommandManager(this);
		registerContexts();

		commandManager.registerCommand(new PathVisualizerCommand());

		registerCompletions();

		new VisualizerHandler();
		new NodeTypeHandler();
		new PathPlayerHandler();
		new RoadMapHandler().loadRoadMaps();

		new GUIHandler(this).enable();

		// Listeners

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new DatabaseListener(database), this);
	}

	@SneakyThrows
	@Override
	public void onDisable() {

		CommandAPI.unregister("nodegroup");
		CommandAPI.unregister("roadmap");
		RoadMapHandler.getInstance().cancelAllEditModes();
		GUIHandler.getInstance().disable();

	}

	private void registerCompletions() {
		CommandCompletions<BukkitCommandCompletionContext> commandCompletions = commandManager.getCommandCompletions();
		commandCompletions.registerCompletion(COMPLETE_ROADMAPS, context -> completeNamespacedKey(context.getInput(), RoadMapHandler.getInstance().getRoadMaps().keySet().stream()));
		commandCompletions.registerCompletion(COMPLETE_ACTIVE_ROADMAPS, context -> completeNamespacedKey(context.getInput(), PathPlayerHandler.getInstance().getPlayer(context.getPlayer().getUniqueId()).getActivePaths().stream()
				.map(path -> path.getRoadMap().getKey())));
		commandCompletions.registerCompletion(COMPLETE_PATH_VISUALIZER, context -> completeNamespacedKey(context.getInput(), VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.filter(v -> v.getPermission() == null || context.getPlayer().hasPermission(v.getPermission()))
				.map(PathVisualizer::getKey)));
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_GROUPS_BY_SELECTION, context -> resolveFromRoadMap(context, roadMap ->
				completeNamespacedKey(context.getInput(), roadMap.getGroups().keySet().stream())));
		commandCompletions.registerCompletion(COMPLETE_GROUPS_BY_PARAMETER, context -> {
			try {
				RoadMap rm = context.getContextValue(RoadMap.class);
				if (rm == null) {
					rm = CommandUtils.getAnyRoadMap(context.getPlayer().getWorld());
				}
				if (rm == null) {
					return null;
				}
				return completeNamespacedKey(context.getInput(), rm.getGroups().keySet().stream());
			} catch (IllegalStateException ignored) {
				return null;
			}
		});
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_GROUPS_BY_PARAMETER, context -> {
			try {
				RoadMap rm = context.getContextValue(RoadMap.class);
				if (rm == null) {
					rm = CommandUtils.getAnyRoadMap(context.getPlayer().getWorld());
				}
				if (rm == null) {
					return null;
				}
				return rm.getGroups().values().stream().filter(NodeGroup::isFindable).map(NodeGroup::getNameFormat).collect(Collectors.toList());
			} catch (IllegalStateException ignored) {
				return null;
			}
		});
		/*TODO commandCompletions.registerCompletion(COMPLETE_FINDABLE_LOCATIONS, context -> {
			PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(context.getPlayer());
			if (pp == null) {
				return null;
			}
			try {
				RoadMap rm = context.getContextValue(RoadMap.class);
				if (rm == null) {
					rm = CommandUtils.getAnyRoadMap(context.getPlayer().getWorld());
				}
				if (rm == null) {
					return null;
				}
				Collection<String> ret = rm.getGroups().values().stream()
						.filter(NodeGroup::isFindable)
						.filter(pp::hasFound)
						.map(NodeGroup::getNameFormat)
						.collect(Collectors.toList());
				ret.addAll(rm.getNodes().stream()
						.filter(n -> n.getGroupKey() != null)
						.filter(pp::hasFound)
						.filter(n -> n instanceof Waypoint)
						.map(Node::getNameFormat)
						.collect(Collectors.toList()));
			} catch (IllegalStateException ignored) {
			}
			return null;
		});*/
		commandCompletions.registerCompletion(COMPLETE_NODE_SELECTION, context ->
				SelectionUtils.completeNodeSelection(context.getInput()));
		/*commandCompletions.registerCompletion(COMPLETE_FINDABLES_CONNECTED, context -> resolveFromRoadMap(context, rm -> {
			Waypoint prev = context.getContextValue(Waypoint.class, 1);
			if (prev == null) {
				return null;
			}
			return prev.getEdges().stream()
					.map(edge -> rm.getNode(edge).getNameFormat())
					.collect(Collectors.toSet());
		}));*/
		/*commandCompletions.registerCompletion(COMPLETE_FINDABLES_FINDABLE, context -> resolveFromRoadMap(context, rm -> rm.getNodes().stream()
				.filter(findable -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.filter(findable -> findable.getPermission() == null || context.getPlayer().hasPermission(findable.getPermission()))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_FOUND, context -> resolveFromRoadMap(context, roadMap -> roadMap.getNodes().stream()
				.filter(findable -> (findable.getGroupKey() != null && !roadMap.getNodeGroup(findable).isFindable()) || PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));*/
		commandCompletions.registerCompletion(COMPLETE_NAVIGABLES, context ->
				resolveFromRoadMap(context, roadMap -> {
					String input = context.getInput();
					int lastIndex = Lists.newArrayList('!', '&', '|', ')', '(').stream()
							.map(input::lastIndexOf).mapToInt(value -> value).max().orElse(0);

					String begin = lastIndex == -1 ? "" : input.substring(0, lastIndex);

					List<String> completions = roadMap.getNavigables().stream()
							.flatMap(navigable -> navigable.getSearchTerms().stream())
							.map(s -> begin + s)
							.collect(Collectors.toList());
					completions.add(input + "&");
					completions.add(input + "|");
					completions.add(input + "(");
					completions.add(input + ")");
					completions.add(input + "!");

					return completions;
				}));
	}

	private Collection<String> resolveFromRoadMap(BukkitCommandCompletionContext context, Function<RoadMap, Collection<String>> fromRoadmap) {
		Player player = context.getPlayer();
		PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player == null ? PathPlayerHandler.CONSOLE_UUID : player.getUniqueId());
		if (pPlayer == null) {
			return null;
		}
		if (pPlayer.getSelectedRoadMap() == null) {
			return Lists.newArrayList("keine Roadmap ausgewählt.");
		}
		RoadMap rm = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMap());
		if (rm == null) {
			return null;
		}
		return fromRoadmap.apply(rm);
	}

	private List<String> completeNamespacedKey(String input, Stream<NamespacedKey> keys) {
		String[] split = input.split(":");
		String namespace = split[0];
		String key = split.length == 2 ? split[1] : "";
		return keys.
				filter(k -> k.getNamespace().startsWith(namespace) || k.getKey().startsWith(key))
				.map(NamespacedKey::toString)
				.collect(Collectors.toList());
	}


	private void registerContexts() {
		CommandContexts<BukkitCommandExecutionContext> contexts = commandManager.getCommandContexts();
		contexts.registerContext(NamespacedKey.class, context -> {
			String search = context.popFirstArg();
			NamespacedKey key = NamespacedKey.fromString(search.toLowerCase());
			if (key == null) {
				throw new InvalidCommandArgument("Keys must be of format '<namespace>:<key>', like 'minecraft:diamond' or 'pathfinder:roadmap1'");
			}
			return key;
		});
		contexts.registerContext(RoadMap.class, context -> {
			String search = context.popFirstArg().toLowerCase();
			NamespacedKey key = NamespacedKey.fromString(search, this);
			if (key == null) {
				throw new InvalidCommandArgument("Roadmap keys must be formatted '<namespace>:<key>', e.g. 'pathfinder:newyork'.");
			}
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(key);
			if (roadMap == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Invalid roadmap: " + search);
			}
			return roadMap;
		});
		contexts.registerContext(NodeGroup.class, context -> {
			String search = context.popFirstArg();
			Player player = context.getPlayer();
			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (pPlayer == null) {
				throw new InvalidCommandArgument("Unknown player '" + player.getName() + "', please contact an administrator.");
			}
			if (pPlayer.getSelectedRoadMap() == null) {
				throw new InvalidCommandArgument("You need to have a roadmap selected to parse node groups.");
			}
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMap());
			if (roadMap == null) {
				throw new InvalidCommandArgument("Your currently selected roadmap is invalid. Please reselect it.");
			}
			NamespacedKey key = NamespacedKey.fromString(search, this);
			if (key == null) {
				throw new InvalidCommandArgument("Not a valid group key: '" + search + "'.");
			}
			return roadMap.getNodeGroup(key);
		});
		contexts.registerContext(NavigateSelection.class, context -> {
			String search = context.popFirstArg();
			Player player = context.getPlayer();
			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (pPlayer == null) {
				throw new InvalidCommandArgument("Unknown player '" + player.getName() + "', please contact an administrator.");
			}
			if (pPlayer.getSelectedRoadMap() == null) {
				throw new InvalidCommandArgument("You need to have a roadmap selected to parse node groups.");
			}
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMap());
			if (roadMap == null) {
				throw new InvalidCommandArgument("Your currently selected roadmap is invalid. Please reselect it.");
			}
			SetArithmeticParser<Navigable> parser = new SetArithmeticParser<>(roadMap.getNavigables(), Navigable::getSearchTerms);
			return new NavigateSelection(roadMap, parser.parse(search));
		});
		contexts.registerContext(NodeSelection.class, context -> {
			String search = context.popFirstArg();
			Player player = context.getPlayer();
			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (pPlayer == null) {
				throw new InvalidCommandArgument("Unknown player '" + player.getName() + "', please contact an administrator.");
			}
			if (pPlayer.getSelectedRoadMap() == null) {
				throw new InvalidCommandArgument("You need to have a roadmap selected to parse nodes.");
			}
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMap());
			if (roadMap == null) {
				throw new InvalidCommandArgument("Your currently selected roadmap is invalid. Please reselect it.");
			}
			return SelectionUtils.getNodeSelection(context.getPlayer(), roadMap.getNodes(), search);
		});
		contexts.registerContext(PathVisualizer.class, context -> {
			String search = context.popFirstArg();
			NamespacedKey key = NamespacedKey.fromString(search);
			if (key == null && !context.isOptional()) {
				throw new InvalidCommandArgument("Invalid Path Visualizer: '" + search + "'.");
			}
			PathVisualizer visualizer = VisualizerHandler.getInstance().getPathVisualizerMap().get(key);
			if (visualizer == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Invalid Path Visualizer: '" + search + "'.");
			}
			return visualizer;
		});

		contexts.registerContext(Double.class, context -> {
			String number = context.popFirstArg();
			if (number.equalsIgnoreCase("null")) {
				return null;
			}
			try {
				Double value = Double.parseDouble(number);
				if (value > Double.MAX_VALUE) {
					return Double.MAX_VALUE;
				}
				if (value < -Double.MAX_VALUE) {
					return -Double.MAX_VALUE;
				}
				return value;
			} catch (NumberFormatException e) {
				throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
			}
		});
	}
}
