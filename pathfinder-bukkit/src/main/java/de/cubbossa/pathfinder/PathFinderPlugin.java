package de.cubbossa.pathfinder;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class PathFinderPlugin extends JavaPlugin {

    @Getter
    private static PathFinderPlugin instance;

    private final BukkitPathFinder pathFinder;

    public PathFinderPlugin() {
        instance = this;
        pathFinder = new BukkitPathFinder(this);
    }

    @Override
    public void onLoad() {
        pathFinder.onLoad();
    }

    @Override
    public void onEnable() {
        pathFinder.onEnable();
    }

    @Override
    public void onDisable() {
        pathFinder.onDisable();
    }
}
