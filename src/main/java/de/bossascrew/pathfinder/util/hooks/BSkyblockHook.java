package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;

public class BSkyblockHook extends Hook {

    @Getter
    private static BSkyblockHook instance;

    public BSkyblockHook(PathPlugin plugin) {
        super(plugin);
        instance = this;
    }
}
