package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;

public abstract class Hook {

    @Getter
    private final PathPlugin plugin;

    public Hook(PathPlugin plugin) {
        this.plugin = plugin;
    }
}
