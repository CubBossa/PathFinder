package pathfinder;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.handler.VisualizerHandler;

public class PathPlugin extends JavaPlugin {

    public static final String PERM_COMMAND_PATHSYSTEM = "bcrew.pathfinder.command.pathsystem.*";


    @Getter
    private RoadMapHandler roadMapHandler;
    @Getter
    private PlayerHandler playerHandler;
    @Getter
    private VisualizerHandler visualizerHandler;

    @Override
    public void onEnable() {
        this.roadMapHandler = new RoadMapHandler();
        this.playerHandler = new PlayerHandler();
        this.visualizerHandler = new VisualizerHandler();
    }

    @Override
    public void onDisable() {

    }
}
