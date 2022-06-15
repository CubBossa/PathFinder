package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.SqlStorage;
import de.bossascrew.pathfinder.data.RoadMap;

public class QuestFindable extends NpcFindable {

    public static final String SCOPE = "QUEST";

    public QuestFindable(int databaseId, RoadMap roadMap, int npcId, String name) {
        super(databaseId, roadMap, npcId, name);
    }

    @Override
    public String getScope() {
        return SCOPE;
    }

    @Override
    void updateData() {
        PluginUtils.getInstance().runAsync(() -> SqlStorage.getInstance().updateFindable(this));
    }
}
