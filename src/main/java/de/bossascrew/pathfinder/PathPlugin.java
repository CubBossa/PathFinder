package de.bossascrew.pathfinder;

import com.google.common.collect.Lists;
import de.bossascrew.acf.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.pathfinder.commands.*;
import de.bossascrew.pathfinder.commands.dependencies.*;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.QuestFindable;
import de.bossascrew.pathfinder.data.findable.TraderFindable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.listener.PlayerListener;
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
import java.util.Objects;
import java.util.stream.Collectors;

public class PathPlugin extends JavaPlugin {

	public static final String PERM_FIND_NODE = "bcrew.pathfinder.find";

	public static final String COMPLETE_ROADMAPS = "@roadmaps";
	public static final String COMPLETE_ACTIVE_ROADMAPS = "@activeroadmaps";
	public static final String COMPLETE_PATH_VISUALIZER = "@path_visualizer";
	public static final String COMPLETE_EDITMODE_VISUALIZER = "@editmode_visualizer";
	public static final String COMPLETE_FINDABLES = "@nodes";
	public static final String COMPLETE_FINDABLES_CONNECTED = "@nodes_connected";
	public static final String COMPLETE_FINDABLES_FINDABLE = "@nodes_findable";
	public static final String COMPLETE_FINDABLES_FOUND = "@nodes_found";
	public static final String COMPLETE_FINDABLE_GROUPS = "@nodegroups";
	public static final String COMPLETE_FINDABLE_FINDABLE_GROUPS = "@nodegroups_findable";
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
	private boolean chestShop = false;
	@Getter
	private boolean quests = false;
	@Getter
	private boolean traders = false;
	@Getter
	private boolean bentobox = false;

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
		if (Bukkit.getPluginManager().isPluginEnabled("DTLTraders")) {
			new TradersHook(this).loadShopsFromDir();
			traders = true;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("BentoBox")) {
			new BSkyblockHook(this);
			bentobox = true;
		}

		new DatabaseModel(this);
		this.visualizerHandler = new VisualizerHandler();
		this.roadMapHandler = new RoadMapHandler();
		this.playerHandler = new PathPlayerHandler();

		registerContexts();

		PaperCommandManager cm = BukkitMain.getInstance().getCommandManager();

		cm.registerCommand(new CancelPath());
		cm.registerCommand(new EditModeVisualizerCommand());
		cm.registerCommand(new FindCommand());
		cm.registerCommand(new NodeGroupCommand());
		cm.registerCommand(new PathVisualizerCommand());
		cm.registerCommand(new RoadMapCommand());
		cm.registerCommand(new WaypointCommand());
		if (traders) {
			cm.registerCommand(new WaypointTraderCommand());
			cm.registerCommand(new FindTraderCommand());
		}
		if (quests) {
			cm.registerCommand(new WaypointQuesterCommand());
			cm.registerCommand(new FindQuesterCommand());
		}
		if (chestShop && bentobox) {
			cm.registerCommand(new FindChestShopsCommand());
		}
		if (traders || quests || (chestShop && bentobox)) {
			cm.registerCommand(new FindItemCommand());
		}

		registerCompletions();

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

	@Override
	public void onDisable() {
		RoadMapHandler.getInstance().cancelAllEditModes();
	}

	private void registerCompletions() {
		BukkitMain bm = BukkitMain.getInstance();
		bm.registerAsyncCompletion(COMPLETE_ROADMAPS, context -> RoadMapHandler.getInstance().getRoadMapsStream()
				.map(RoadMap::getName)
				.collect(Collectors.toSet()));
		bm.registerAsyncCompletion(COMPLETE_ACTIVE_ROADMAPS, context -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer().getUniqueId()).getActivePaths().stream()
				.map(path -> RoadMapHandler.getInstance().getRoadMap(path.getRoadMap().getDatabaseId()))
				.filter(Objects::nonNull)
				.map(RoadMap::getName)
				.collect(Collectors.toSet()));
		bm.registerAsyncCompletion(COMPLETE_PATH_VISUALIZER, context -> VisualizerHandler
				.getInstance().getPathVisualizerStream()
				.map(PathVisualizer::getName)
				.collect(Collectors.toSet()));
		bm.registerAsyncCompletion(COMPLETE_EDITMODE_VISUALIZER, context -> VisualizerHandler
				.getInstance().getEditModeVisualizerStream()
				.map(EditModeVisualizer::getName)
				.collect(Collectors.toSet()));
		bm.registerAsyncCompletion(COMPLETE_FINDABLE_GROUPS, context -> resolveFromRoadMap(context, roadMap ->
				roadMap.getGroups().values().stream()
						.map(FindableGroup::getName)
						.collect(Collectors.toSet())));
		bm.registerAsyncCompletion(COMPLETE_FINDABLE_FINDABLE_GROUPS, context -> resolveFromRoadMap(context, roadMap ->
				roadMap.getGroups().values().stream() //TODO ohne selection
						.filter(FindableGroup::isFindable)
						.map(FindableGroup::getName)
						.collect(Collectors.toSet())));
		bm.registerAsyncCompletion(COMPLETE_FINDABLES, context -> resolveFromRoadMap(context, roadMap ->
				roadMap.getFindables().stream()
						.map(Findable::getName)
						.collect(Collectors.toSet())));
		bm.registerAsyncCompletion(COMPLETE_FINDABLES_CONNECTED, context -> resolveFromRoadMap(context, rm -> {
			Findable prev = context.getContextValue(Findable.class, 1);
			if (prev == null) {
				return null;
			}
			return prev.getEdges().stream()
					.map(edge -> rm.getFindable(edge).getName())
					.collect(Collectors.toSet());
		}));
		bm.registerAsyncCompletion(COMPLETE_FINDABLES_FINDABLE, context -> resolveFromRoadMap(context, rm -> rm.getFindables().stream()
				.filter(findable -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable.getDatabaseId()))
				.filter(findable -> findable.getPermission() == null || context.getPlayer().hasPermission(findable.getPermission()))
				.map(Findable::getName)
				.collect(Collectors.toSet())));
		bm.registerAsyncCompletion(COMPLETE_FINDABLES_FOUND, context -> resolveFromRoadMap(context, roadMap -> roadMap.getFindables().stream()
				.filter(findable -> (findable.getGroup() != null && !findable.getGroup().isFindable()) || PathPlayerHandler.getInstance().getPlayer(context.getPlayer()).hasFound(findable.getDatabaseId()))
				.map(Findable::getName)
				.collect(Collectors.toSet())));
		bm.registerAsyncCompletion(COMPLETE_TRADERS, context -> {
			RoadMap roadMap = context.getContextValue(RoadMap.class, 1);
			return roadMap.getFindables().stream()
					.filter(findable -> findable instanceof TraderFindable)
					.map(Findable::getName)
					.collect(Collectors.toSet());
		});
		bm.registerAsyncCompletion(COMPLETE_QUESTERS, context -> {
			RoadMap roadMap = context.getContextValue(RoadMap.class, 1);
			return roadMap.getFindables().stream()
					.filter(findable -> findable instanceof QuestFindable)
					.map(Findable::getName)
					.collect(Collectors.toSet());
		});
	}

	private interface Converter<A, B> {
		B convert(A a);
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
		CommandContexts<BukkitCommandExecutionContext> cm = BukkitMain.getInstance().getCommandManager().getCommandContexts();
		cm.registerContext(RoadMap.class, context -> {
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
		cm.registerContext(PathVisualizer.class, context -> {
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
		cm.registerContext(EditModeVisualizer.class, context -> {
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
		cm.registerContext(Findable.class, this::resolveFindable);
		cm.registerContext(Node.class, this::resolveFindable);
		cm.registerContext(FindableGroup.class, context -> {
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
			FindableGroup group = roadMap.getFindableGroup(search);
			if (group == null) {
				if (context.isOptional()) {
					return null;
				}
				throw new InvalidCommandArgument("Diese Gruppe existiert nicht.");
			}
			return group;
		});
		cm.registerContext(Double.class, context -> {
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
		Node findable = (Node) roadMap.getFindable(search);
		if (findable == null) {
			if (context.isOptional()) {
				return null;
			}
			throw new InvalidCommandArgument("Diese Node existiert nicht.");
		}
		return findable;
	}
}
