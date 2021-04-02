package pathfinder;

import de.bossascrew.acf.CommandManager;
import de.bossascrew.acf.InvalidCommandArgument;
import de.bossascrew.acf.MessageKeys;
import de.bossascrew.core.BukkitMain;
import lombok.Getter;
import net.bytebuddy.pool.TypePool;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.handler.VisualizerHandler;
import pathfinder.inventory.HotbarMenuHandler;
import pathfinder.visualisation.EditModeVisualizer;
import pathfinder.visualisation.PathVisualizer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PathPlugin extends JavaPlugin {

    public static final String COMPLETE_ROADMAPS = "@roadmaps";
    public static final String COMPLETE_VISUALIZER = "@visualizer";
    public static final String COMPLETE_EDITMODE_VISUALIZER = "@visualizer";
    public static final String COMPLETE_PARTICLES = "@particles";

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

        registerContexts();

        this.visualizerHandler = new VisualizerHandler();
        this.hotbarMenuHandler = new HotbarMenuHandler(this);
        this.roadMapHandler = new RoadMapHandler();
        this.playerHandler = new PlayerHandler();

        registerCompletions();
    }

    @Override
    public void onDisable() {

    }

    private void registerCompletions() {
        BukkitMain.getInstance().registerAsyncCompletion(COMPLETE_ROADMAPS, context -> RoadMapHandler.getInstance().getRoadMapsStream()
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

    }

    private void registerContexts() {
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(RoadMap.class, context -> {
            String search = context.popFirstArg();

            RoadMap roadMap = roadMapHandler.getRoadMap(search);
            if (roadMap == null) {
                throw new InvalidCommandArgument("Ungültige Roadmap");
            }
            return roadMap;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(PathVisualizer.class, context -> {
            String search = context.popFirstArg();

            PathVisualizer visualizer = visualizerHandler.getPathVisualizer(search);
            if (visualizer == null) {
                throw new InvalidCommandArgument("Ungültiger Pfad-Visualisierer");
            }
            return visualizer;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(EditModeVisualizer.class, context -> {
            String search = context.popFirstArg();

            EditModeVisualizer visualizer = visualizerHandler.getEditVisualizer(search);
            if (visualizer == null) {
                throw new InvalidCommandArgument("Ungültiger EditMode-Visualisierer");
            }
            return visualizer;
        });
        BukkitMain.getInstance().getCommandManager().getCommandContexts().registerContext(Particle.class, context -> {
            String search = context.popFirstArg();
            Particle particle = null;
            try {
                particle = Particle.valueOf(search);
            } catch (IllegalArgumentException e) {
                throw new InvalidCommandArgument("Ungültige Partikel");
            }
            return particle;
        });
    }
}
