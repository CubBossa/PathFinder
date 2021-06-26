package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;

public abstract class Hook {

    @Getter
    private static Hook instance;

    @Getter
    private final PathPlugin plugin;

    public Hook(PathPlugin plugin) {
        instance = this;
        this.plugin = plugin;
    }
}
