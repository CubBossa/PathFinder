package pathfinder;

import de.bossascrew.acf.CommandManager;
import de.bossascrew.acf.InvalidCommandArgument;
import de.bossascrew.acf.MessageKeys;
import de.bossascrew.core.BukkitMain;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.handler.VisualizerHandler;
import pathfinder.inventory.HotbarMenuHandler;
import pathfinder.visualisation.EditModeVisualizer;
import pathfinder.visualisation.PathVisualizer;

public class PathPlugin extends JavaPlugin {

    public static final String PERM_COMMAND_PATHSYSTEM = "bcrew.pathfinder.command.pathsystem.*";
    public static final String COMPLETE_ROADMAPS = "@roadmaps";
    public static final String COMPLETE_VISUALIZER = "@visualizer";
    public static final String COMPLETE_EDITMODE_VISUALIZER = "@visualizer";

    public static final String PREFIX = ChatColor.BLUE + "Pathfinder " + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY;

    @Getter
    private static PathPlugin instance;

    @Getter
    private HotbarMenuHandler hotbarMenuHandler;
    @Getter
    private RoadMapHandler roadMapHandler;
    @Getter
    private PlayerHandler playerHandler;
    @Getter
    private VisualizerHandler visualizerHandler;

    @Override
    public void onEnable() {
        instance = this;

        registerCompletions();

        this.visualizerHandler = new VisualizerHandler();
        this.hotbarMenuHandler = new HotbarMenuHandler(this);
        this.roadMapHandler = new RoadMapHandler();
        this.playerHandler = new PlayerHandler();

        registerContexts();
    }

    @Override
    public void onDisable() {

    }

    private void registerCompletions() {

    }

    private void registerContexts() {
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(RoadMap.class, context -> {
            String search = context.popFirstArg();

            RoadMap roadMap = roadMapHandler.getRoadMap(search);
            if (roadMap == null) {
                //TODO richtiges MessageKeys
                throw new InvalidCommandArgument(MessageKeys.INVALID_SYNTAX, "{search}", search);
            }
            return roadMap;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(PathVisualizer.class, context -> {
            String search = context.popFirstArg();

            PathVisualizer visualizer = visualizerHandler.getPathVisualizer(search);
            if (visualizer == null) {
                //TODO richtiges MessageKeys
                throw new InvalidCommandArgument(MessageKeys.INVALID_SYNTAX, "{search}", search);
            }
            return visualizer;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(EditModeVisualizer.class, context -> {
            String search = context.popFirstArg();

            EditModeVisualizer visualizer = visualizerHandler.getEditVisualizer(search);
            if (visualizer == null) {
                //TODO richtiges MessageKeys
                throw new InvalidCommandArgument(MessageKeys.INVALID_SYNTAX, "{search}", search);
            }
            return visualizer;
        });
    }
}
