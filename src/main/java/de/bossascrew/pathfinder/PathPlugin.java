package de.bossascrew.pathfinder;

import co.aikar.commands.*;
import com.google.common.collect.Lists;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.pathfinder.commands.*;
import de.bossascrew.pathfinder.commands.dependencies.*;
import de.bossascrew.pathfinder.data.*;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.QuestFindable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.listener.PlayerListener;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.bossascrew.pathfinder.util.hooks.BSkyblockHook;
import de.bossascrew.pathfinder.util.hooks.ChestShopHook;
import de.bossascrew.pathfinder.util.hooks.QuestsHook;
import de.bossascrew.pathfinder.util.hooks.TradersHook;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathPlugin extends JavaPlugin {

	public static final String NBT_ARMORSTAND_KEY = "pathfinder_armorstand";

	public static final String PERM_FIND_NODE = "bcrew.pathfinder.find";
	public static final String PERM_COMMAND_FIND_INFO = "pathfinder.command.find.info";
	public static final String PERM_COMMAND_FIND_STYLE = "pathfinder.command.find.style";
	public static final String PERM_COMMAND_FIND_ITEMS = "pathfinder.command.find.items";
	public static final String PERM_COMMAND_FIND_LOCATIONS = "pathfinder.command.find.location";
	public static final String PERM_COMMAND_FIND_QUESTS = "pathfinder.command.find.quest";
	public static final String PERM_COMMAND_FIND_TRADERS = "pathfinder.command.find.trader";
	public static final String PERM_COMMAND_FIND_CHESTSHOPS = "pathfinder.command.find.chestshops";

	public static final String COMPLETE_ROADMAPS = "@roadmaps";
	public static final String COMPLETE_ACTIVE_ROADMAPS = "@activeroadmaps";
	public static final String COMPLETE_PATH_VISUALIZER = "@path_visualizer";
	public static final String COMPLETE_PATH_VISUALIZER_STYLES = "@path_visualizer_styles";
	public static final String COMPLETE_EDITMODE_VISUALIZER = "@editmode_visualizer";
	public static final String COMPLETE_FINDABLES = "@nodes";
	public static final String COMPLETE_FINDABLES_CONNECTED = "@nodes_connected";
	public static final String COMPLETE_FINDABLES_FINDABLE = "@nodes_findable";
	public static final String COMPLETE_FINDABLES_FOUND = "@nodes_found";
	public static final String COMPLETE_GROUPS_BY_PARAMETER = "@nodegroups_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_PARAMETER = "@nodegroups_findable_parametered";
	public static final String COMPLETE_FINDABLE_GROUPS_BY_SELECTION = "@nodegroups_findable_selection";
	public static final String COMPLETE_FINDABLE_LOCATIONS = "@findable_locations";
	public static final String COMPLETE_TRADERS = "@nodes_traders";
	public static final String COMPLETE_QUESTERS = "@nodes_questers";

	public static final int COLOR_LIGHT_INT = 0x7F7FFF;
	public static final int COLOR_DARK_INT = 0x5555FF;

	public static final TextColor COLOR_LIGHT = TextColor.color(COLOR_LIGHT_INT);
	public static final TextColor COLOR_DARK = TextColor.color(COLOR_DARK_INT);
	public static final ChatColor CHAT_COLOR_LIGHT = ChatColor.of(new Color(COLOR_LIGHT_INT));
	public static final ChatColor CHAT_COLOR_DARK = ChatColor.of(new Color(COLOR_DARK_INT));

	public static final String PREFIX = CHAT_COLOR_DARK + "" + ChatColor.BOLD + "|" + ChatColor.GRAY + " ";
	public static final Component PREFIX_COMP = Component.empty().append(Component.text("|", COLOR_DARK, TextDecoration.BOLD)
			.append(Component.text(" ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)));

	@Getter
	private static PathPlugin instance;
	@Getter
	private RoadMapHandler roadMapHandler;
	@Getter
	private PathPlayerHandler playerHandler;
	@Getter
	private VisualizerHandler visualizerHandler;
	@Getter
	private DataStorage database;

	@Getter
	private boolean chestShop = false;
	@Getter
	private boolean quests = false;
	@Getter
	private boolean traders = false;
	@Getter
	private boolean bentobox = false;

	private BukkitCommandManager commandManager;

	@Override
	public void onEnable() {
		instance = this;
		if (Bukkit.getPluginManager().isPluginEnabled("ChestShopLogger")) {
			new ChestShopHook(this);
			chestShop = true;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("Quests")) {
			new QuestsHook(this);
			quests = true;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("dtlTraders") || Bukkit.getPluginManager().isPluginEnabled("dtlTradersPlus")) {
			new TradersHook(this).loadShopsFromDir();
			traders = true;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("BentoBox")) {
			new BSkyblockHook(this);
			bentobox = true;
		}

		new SqlStorage(this);
		this.visualizerHandler = new VisualizerHandler();
		this.roadMapHandler = new RoadMapHandler();
		this.playerHandler = new PathPlayerHandler();

		commandManager = new BukkitCommandManager(this);
		registerContexts();

		commandManager.registerCommand(new PathFinderCommand());
		commandManager.registerCommand(new CancelPath());
		commandManager.registerCommand(new EditModeVisualizerCommand());
		commandManager.registerCommand(new FindCommand());
		commandManager.registerCommand(new NodeGroupCommand());
		commandManager.registerCommand(new PathVisualizerCommand());
		commandManager.registerCommand(new RoadMapCommand());
		commandManager.registerCommand(new WaypointCommand());
		if (traders) {
			commandManager.registerCommand(new WaypointTraderCommand());
			commandManager.registerCommand(new FindTraderCommand());
		}
		if (quests) {
			commandManager.registerCommand(new WaypointQuesterCommand());
			commandManager.registerCommand(new FindQuesterCommand());
		}
		if (chestShop && bentobox) {
			commandManager.registerCommand(new FindChestShopsCommand());
		}
		if (traders || quests || (chestShop && bentobox)) {
			commandManager.registerCommand(new FindItemCommand());
		}

		registerCompletions();

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

	@Override
	public void onDisable() {
		RoadMapHandler.getInstance().cancelAllEditModes();
	}

	private void registerCompletions() {
		CommandCompletions<BukkitCommandCompletionContext> commandCompletions = commandManager.getCommandCompletions();
		commandCompletions.registerCompletion(COMPLETE_ROADMAPS, context -> RoadMapHandler.getInstance().getRoadMapsStream()
				.map(RoadMap::getNameFormat)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_ACTIVE_ROADMAPS, context -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer().getUniqueId()).getActivePaths().stream()
				.map(path -> RoadMapHandler.getInstance().getRoadMap(path.getRoadMap().getRoadmapId()))
				.filter(Objects::nonNull)
				.map(RoadMap::getNameFormat)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_PATH_VISUALIZER, context -> VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.map(PathVisualizer::getName)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_PATH_VISUALIZER_STYLES, context -> VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.filter(PathVisualizer::isPickable)
				.map(PathVisualizer::getName)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_EDITMODE_VISUALIZER, context -> VisualizerHandler
				.getInstance().getEditModeVisualizerStream()
				.map(EditModeVisualizer::getName)
				.collect(Collectors.toSet()));
		commandCompletions.registerCompletion(COMPLETE_FINDABLE_GROUPS_BY_SELECTION, context -> resolveFromRoadMap(context, roadMap ->
				roadMap.getGroups().values().stream()
						.map(FindableGroup::getName)
						.collect(Collectors.toSet())));
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
			return rm.getGroups().values().stream().map(FindableGroup::getName).collect(Collectors.toList());
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
			return rm.getGroups().values().stream().filter(FindableGroup::isFindable).map(FindableGroup::getName).collect(Collectors.toList());
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
					.filter(FindableGroup::isFindable)
					.filter(g -> pp.hasFound(g.getDatabaseId(), true))
					.map(FindableGroup::getName)
					.collect(Collectors.toList());
			ret.addAll(rm.getFindables().stream()
					.filter(f -> f.getGroup() == null)
					.filter(f -> pp.hasFound(f.getNodeId(), false))
					.filter(f -> f instanceof Node)
					.map(Node::getNameFormat)
					.collect(Collectors.toList()));
			return ret;
		});
		commandCompletions.registerCompletion(COMPLETE_FINDABLES, context -> resolveFromRoadMap(context, roadMap ->
				SelectionUtils.complete(roadMap.getFindables(), context.getInput())));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_CONNECTED, context -> resolveFromRoadMap(context, rm -> {
			Node prev = context.getContextValue(Node.class, 1);
			if (prev == null) {
				return null;
			}
			return prev.getEdges().stream()
					.map(edge -> rm.getFindable(edge).getNameFormat())
					.collect(Collectors.toSet());
		}));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_FINDABLE, context -> resolveFromRoadMap(context, rm -> rm.getFindables().stream()
				.filter(findable -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.filter(findable -> findable.getPermission() == null || context.getPlayer().hasPermission(findable.getPermission()))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));
		commandCompletions.registerCompletion(COMPLETE_FINDABLES_FOUND, context -> resolveFromRoadMap(context, roadMap -> roadMap.getFindables().stream()
				.filter(findable -> (findable.getGroup() != null && !findable.getGroup().isFindable()) || PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable))
				.map(Node::getNameFormat)
				.collect(Collectors.toSet())));
		commandCompletions.registerCompletion(COMPLETE_TRADERS, context -> {
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
			PathPlayer player = PathPlayerHandler.getInstance().getPlayer(context.getPlayer());
			return rm.getFindables().stream()
					.filter(findable -> findable instanceof TraderFindable)
					.filter(player::hasFound)
					.map(Node::getNameFormat)
					.collect(Collectors.toSet());
		});
		commandCompletions.registerCompletion(COMPLETE_QUESTERS, context -> {
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
			return rm.getFindables().stream()
					.filter(findable -> findable instanceof QuestFindable)
					.map(Node::getNameFormat)
					.collect(Collectors.toSet());
		});
	}

	private Collection<String> resolveFromRoadMap(BukkitCommandCompletionContext context, Converter<RoadMap, Collection<String>> fromRoadmap) {
		Player player = context.getPlayer();
		PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
		if (pPlayer == null) {
			return null;
		}
		if (pPlayer.getSelectedRoadMapId() == null) {
			return Lists.newArrayList("keine Roadmap ausgewählt.");
		}
		RoadMap rm = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());
		if (rm == null) {
			return null;
		}
		return fromRoadmap.convert(rm);
	}

	private void registerContexts() {
		CommandContexts<BukkitCommandExecutionContext> commandManager = BukkitMain.getInstance().getCommandManager().getCommandContexts();
		commandManager.registerContext(RoadMap.class, context -> {
			String search = context.popFirstArg();

			RoadMap roadMap = roadMapHandler.getRoadMap(search);
			if (roadMap == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Ungültige Roadmap: " + search);
			}
			return roadMap;
		});
		commandManager.registerContext(NodeSelection.class, context -> {
			String search = context.popFirstArg();
			Player player = context.getPlayer();
			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());
			return SelectionUtils.getTargetSelection(context.getPlayer(), roadMap.getFindables(), search);
			//TODO exception handling
		});
		commandManager.registerContext(PathVisualizer.class, context -> {
			String search = context.popFirstArg();

			PathVisualizer visualizer = visualizerHandler.getPathVisualizer(search);
			if (visualizer == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Ungültiger Pfad-Visualisierer.");
			}
			return visualizer;
		});
		commandManager.registerContext(EditModeVisualizer.class, context -> {
			String search = context.popFirstArg();

			EditModeVisualizer visualizer = visualizerHandler.getEditModeVisualizer(search);
			if (visualizer == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Ungültiger EditMode-Visualisierer.");
			}
			return visualizer;
		});
		commandManager.registerContext(Node.class, this::resolveFindable);
		commandManager.registerContext(Node.class, context -> (Node) resolveFindable(context));
		commandManager.registerContext(FindableGroup.class, context -> {
			String search = context.popFirstArg();
			Player player = context.getPlayer();
			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (pPlayer == null) {
				return null;
			}
			RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());

			List<FindableGroup> possibleResults = roadMapHandler.getRoadMapsStream()
					.map(rm -> rm.getFindableGroup(search))
					.collect(Collectors.toList());

			FindableGroup ret;
			if (roadMap != null) {
				//Ausgewählte Roadmap bevorzugen
				ret = possibleResults.stream()
						.filter(Objects::nonNull)
						.filter(g -> g.getRoadMap().getRoadmapId() == roadMap.getRoadmapId())
						.findFirst().orElse(null);
			} else {
				ret = possibleResults.stream().findAny().orElse(null);
			}

			if (ret == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Diese Gruppe existiert nicht.");
			}
			return ret;
		});
		commandManager.registerContext(Double.class, context -> {
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

	private Node resolveFindable(BukkitCommandExecutionContext context) {
		String search = context.popFirstArg();
		Player player = context.getPlayer();
		PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
		if (pPlayer == null) {
			return null;
		}
		RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());
		if (roadMap == null) {
			throw new InvalidCommandArgument("Du musst eine RoadMap auswählen. (/roadmap select)");
		}
		Node findable = roadMap.getFindable(search);
		if (findable == null) {
			if (context.isOptional()) {
				return null;
			}
			throw new InvalidCommandArgument("Diese Node existiert nicht.");
		}
		return findable;
	}
}
