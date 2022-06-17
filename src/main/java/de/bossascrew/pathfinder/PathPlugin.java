package de.bossascrew.pathfinder;

import co.aikar.commands.*;
import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.commands.*;
import de.bossascrew.pathfinder.data.DataStorage;
import de.bossascrew.pathfinder.data.InMemoryDatabase;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.listener.PlayerListener;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import de.bossascrew.splinelib.SplineLib;
import de.bossascrew.splinelib.util.BezierVector;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathPlugin extends JavaPlugin {

	public static final String PERM_FIND_NODE = "bcrew.pathfinder.find";
	public static final String PERM_COMMAND_FIND_INFO = "pathfinder.command.find.info";
	public static final String PERM_COMMAND_FIND_STYLE = "pathfinder.command.find.style";
	public static final String PERM_COMMAND_FIND_ITEMS = "pathfinder.command.find.items";
	public static final String PERM_COMMAND_FIND_LOCATIONS = "pathfinder.command.find.location";
	public static final String PERM_COMMAND_FIND_QUESTS = "pathfinder.command.find.quest";
	public static final String PERM_COMMAND_FIND_TRADERS = "pathfinder.command.find.trader";

	public static final String COMPLETE_ROADMAPS = "@roadmaps";
	public static final String COMPLETE_ACTIVE_ROADMAPS = "@activeroadmaps";
	public static final String COMPLETE_PATH_VISUALIZER = "@path_visualizer";
	public static final String COMPLETE_PATH_VISUALIZER_STYLES = "@path_visualizer_styles";
	public static final String COMPLETE_EDITMODE_VISUALIZER = "@editmode_visualizer";
	public static final String COMPLETE_NODE_SELECTION = "@nodes";
	public static final String COMPLETE_FINDABLES_CONNECTED = "@nodes_connected";
	public static final String COMPLETE_FINDABLES_FINDABLE = "@nodes_findable";
	public static final String COMPLETE_FINDABLES_FOUND = "@nodes_found";
	public static final String COMPLETE_GROUPS_BY_PARAMETER = "@nodegroups_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_PARAMETER = "@nodegroups_findable_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_SELECTION = "@nodegroups_findable_selection";
	public static final String COMPLETE_FINDABLE_LOCATIONS = "@findable_locations";

	public static final int COLOR_LIGHT_INT = 0x7F7FFF;
	public static final int COLOR_DARK_INT = 0x5555FF;

	public static final TextColor COLOR_LIGHT = TextColor.color(COLOR_LIGHT_INT);
	public static final TextColor COLOR_DARK = TextColor.color(COLOR_DARK_INT);
	public static final ChatColor CHAT_COLOR_LIGHT = ChatColor.of(new Color(COLOR_LIGHT_INT));
	public static final ChatColor CHAT_COLOR_DARK = ChatColor.of(new Color(COLOR_DARK_INT));

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


	@SneakyThrows
	@Override
	public void onEnable() {
		instance = this;

		// Data

		TranslationHandler translationHandler = new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"));
		translationHandler.registerAnnotatedLanguageClass(Messages.class);
		translationHandler.loadLanguages();

		database = new InMemoryDatabase(this.getLogger());

		// Commands

		commandManager = new BukkitCommandManager(this);
		registerContexts();

		commandManager.registerCommand(new PathFinderCommand());
		commandManager.registerCommand(new CancelPath());
		commandManager.registerCommand(new FindCommand());
		commandManager.registerCommand(new NodeGroupCommand());
		commandManager.registerCommand(new PathVisualizerCommand());
		commandManager.registerCommand(new RoadMapCommand());
		commandManager.registerCommand(new WaypointCommand());

		registerCompletions();

		// Listeners

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

	@Override
	public void onDisable() {

	}

	private void registerCompletions() {
		CommandCompletions<BukkitCommandCompletionContext> commandCompletions = commandManager.getCommandCompletions();
		commandCompletions.registerCompletion(COMPLETE_ROADMAPS, context -> completeNamespacedKey(context.getInput(), RoadMapHandler.getInstance().getRoadMaps().keySet().stream()));
		commandCompletions.registerCompletion(COMPLETE_ACTIVE_ROADMAPS, context -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer().getUniqueId()).getActivePaths().stream()
				.map(path -> RoadMapHandler.getInstance().getRoadMap(path.getRoadMap().getKey()))
				.filter(Objects::nonNull)
				.map(RoadMap::getNameFormat)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_PATH_VISUALIZER, context -> VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.map(SimpleCurveVisualizer::getNameFormat)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_PATH_VISUALIZER_STYLES, context -> VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.filter(vs -> vs.getPermission() == null || context.getPlayer().hasPermission(vs.getPermission()))
				.map(SimpleCurveVisualizer::getNameFormat)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_GROUPS_BY_SELECTION, context -> resolveFromRoadMap(context, roadMap ->
				completeNamespacedKey(context.getInput(), roadMap.getGroups().keySet().stream())));
		commandCompletions.registerCompletion(COMPLETE_GROUPS_BY_PARAMETER, context -> {
			RoadMap rm = null;
			try {
				rm = context.getContextValue(RoadMap.class);
			} catch (IllegalStateException ignored) {
			}
			if (rm == null) {
				rm = CommandUtils.getAnyRoadMap(context.getPlayer().getWorld());
			}
			if (rm == null) {
				return null;
			}
			return completeNamespacedKey(context.getInput(), rm.getGroups().keySet().stream());
		});
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_GROUPS_BY_PARAMETER, context -> {
			RoadMap rm = null;
			try {
				rm = context.getContextValue(RoadMap.class);
			} catch (IllegalStateException ignored) {
			}
			if (rm == null) {
				rm = CommandUtils.getAnyRoadMap(context.getPlayer().getWorld());
			}
			if (rm == null) {
				return null;
			}
			return rm.getGroups().values().stream().filter(NodeGroup::isFindable).map(NodeGroup::getNameFormat).collect(Collectors.toList());
		});
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_LOCATIONS, context -> {
			PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(context.getPlayer());
			if (pp == null) {
				return null;
			}
			RoadMap rm = null;
			try {
				rm = context.getContextValue(RoadMap.class);
			} catch (IllegalStateException ignored) {
			}
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
			return ret;
		});
		commandCompletions.registerCompletion(COMPLETE_NODE_SELECTION, context -> resolveFromRoadMap(context, roadMap ->
				SelectionUtils.completeNodeSelection(roadMap.getNodes(), context.getInput())));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_CONNECTED, context -> resolveFromRoadMap(context, rm -> {
			Waypoint prev = context.getContextValue(Waypoint.class, 1);
			if (prev == null) {
				return null;
			}
			return prev.getEdges().stream()
					.map(edge -> rm.getNode(edge).getNameFormat())
					.collect(Collectors.toSet());
		}));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_FINDABLE, context -> resolveFromRoadMap(context, rm -> rm.getNodes().stream()
				.filter(findable -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.filter(findable -> findable.getPermission() == null || context.getPlayer().hasPermission(findable.getPermission()))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_FOUND, context -> resolveFromRoadMap(context, roadMap -> roadMap.getNodes().stream()
				.filter(findable -> (findable.getGroupKey() != null && !roadMap.getNodeGroup(findable).isFindable()) || PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));
	}

	private Collection<String> resolveFromRoadMap(BukkitCommandCompletionContext context, Function<RoadMap, Collection<String>> fromRoadmap) {
		Player player = context.getPlayer();
		PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
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
			NamespacedKey key = NamespacedKey.fromString(search);
			if (key == null) {
				throw new InvalidCommandArgument("Keys must be of format '<namespace>:<key>', like 'minecraft:diamond' or 'pathfinder:roadmap1'");
			}
			return key;
		});
		contexts.registerContext(RoadMap.class, context -> {
			String search = context.popFirstArg();
			NamespacedKey key = NamespacedKey.fromString(search);
			if(key == null) {
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
		contexts.registerContext(SimpleCurveVisualizer.class, context -> {
			String search = context.popFirstArg();

			SimpleCurveVisualizer visualizer = VisualizerHandler.getInstance().getPathVisualizer(search);
			if (visualizer == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Ungültiger Pfad-Visualisierer.");
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
