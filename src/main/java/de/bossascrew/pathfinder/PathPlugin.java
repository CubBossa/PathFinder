package de.bossascrew.pathfinder;

import de.bossascrew.acf.InvalidCommandArgument;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.pathfinder.commands.*;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.inventory.HotbarMenuHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathPlugin extends JavaPlugin {

    public static final String PERM_FIND_NODE = "bcrew.pathfinder.find";

    public static final String COMPLETE_ROADMAPS = "@roadmaps";
    public static final String COMPLETE_ACTIVE_ROADMAPS = "@activeroadmaps";
    public static final String COMPLETE_VISUALIZER = "@visualizer";
    public static final String COMPLETE_EDITMODE_VISUALIZER = "@visualizer";
    public static final String COMPLETE_PARTICLES = "@particles";
    public static final String COMPLETE_NODES = "@nodes";
    public static final String COMPLETE_NODE_GROUPS = "@nodegroups";

    public static final String PREFIX = ChatColor.BLUE + "Pathfinder " + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY;


    @Getter
    private static PathPlugin instance;

    @Getter
    private HotbarMenuHandler hotbarMenuHandler;
    @Getter
    private RoadMapHandler roadMapHandler;
    @Getter
    private PathPlayerHandler playerHandler;
    @Getter
    private VisualizerHandler visualizerHandler;

    @Override
    public void onEnable() {
        instance = this;

        new DatabaseModel(this);
        this.visualizerHandler = new VisualizerHandler();
        this.roadMapHandler = new RoadMapHandler();
        this.playerHandler = new PathPlayerHandler();

        registerContexts();

        BukkitMain.getInstance().getCommandManager().registerCommand(new CancelPath());
        BukkitMain.getInstance().getCommandManager().registerCommand(new EditModeVisualizerCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new FindeCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new NodeGroupCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new PathSystemCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new PathVisualizerCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new RoadMapCommand());
        BukkitMain.getInstance().getCommandManager().registerCommand(new WaypointCommand());
        registerCompletions();
    }

    @Override
    public void onDisable() {

    }

    private void registerCompletions() {
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_ROADMAPS, context -> RoadMapHandler.getInstance().getRoadMapsStream()
                .map(RoadMap::getName)
                .collect(Collectors.toSet()));
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_ACTIVE_ROADMAPS, context -> PathPlayerHandler.getInstance().getPlayer(context.getPlayer().getUniqueId()).getActivePaths().stream()
                .map(path -> RoadMapHandler.getInstance().getRoadMap(path.getRoadMap().getDatabaseId()))
                .filter(Objects::nonNull)
                .map(RoadMap::getName)
                .collect(Collectors.toSet()));
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_VISUALIZER, context -> VisualizerHandler
                .getInstance().getPathVisualizers()
                .map(PathVisualizer::getName)
                .collect(Collectors.toSet()));
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_EDITMODE_VISUALIZER, context -> VisualizerHandler
                .getInstance().getEditModeVisualizer()
                .map(EditModeVisualizer::getName)
                .collect(Collectors.toSet()));
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_PARTICLES, context -> Arrays.stream(Particle.values())
                .map(Particle::name)
                .collect(Collectors.toSet()));
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_NODE_GROUPS, context -> {
            Player player = context.getPlayer();
            PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
            if (pPlayer == null) {
                return null;
            }
            RoadMap rm = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());
            if (rm == null) {
                return null;
            }
            return rm.getGroups().stream()
                    .map(FindableGroup::getName)
                    .collect(Collectors.toSet());
        });
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_NODES, context -> {
            Player player = context.getPlayer();
            PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
            if (pPlayer == null) {
                return null;
            }
            RoadMap rm = RoadMapHandler.getInstance().getRoadMap(pPlayer.getSelectedRoadMapId());
            if (rm == null) {
                return null;
            }
            return rm.getFindables().stream()
                    .map(Findable::getName)
                    .collect(Collectors.toSet());
        });
    }

    private void registerContexts() {
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(RoadMap.class, context -> {
            String search = context.popFirstArg();

            RoadMap roadMap = roadMapHandler.getRoadMap(search);
            if (roadMap == null) {
                throw new InvalidCommandArgument("Ungültige Roadmap.");
            }
            return roadMap;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(PathVisualizer.class, context -> {
            String search = context.popFirstArg();

            PathVisualizer visualizer = visualizerHandler.getPathVisualizer(search);
            if (visualizer == null) {
                throw new InvalidCommandArgument("Ungültiger Pfad-Visualisierer.");
            }
            return visualizer;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(EditModeVisualizer.class, context -> {
            String search = context.popFirstArg();

            EditModeVisualizer visualizer = visualizerHandler.getEditVisualizer(search);
            if (visualizer == null) {
                throw new InvalidCommandArgument("Ungültiger EditMode-Visualisierer.");
            }
            return visualizer;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(Particle.class, context -> {
            String search = context.popFirstArg();
            Particle particle = null;
            try {
                particle = Particle.valueOf(search);
            } catch (IllegalArgumentException e) {
                throw new InvalidCommandArgument("Ungültige Partikel.");
            }
            return particle;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(Findable.class, context -> {
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
            Findable findable = roadMap.getFindable(search);
            if (findable == null) {
                throw new InvalidCommandArgument("Diese Node existiert nicht.");
            }
            return findable;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(FindableGroup.class, context -> {
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
                throw new InvalidCommandArgument("Diese Gruppe existiert nicht.");
            }
            return group;
        });
    }
}
