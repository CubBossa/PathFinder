package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;

public class QuestsHook extends Hook {

    @Getter
    private static QuestsHook instance;

    public QuestsHook(PathPlugin plugin) {
        super(plugin);
        instance = this;
    }
}
