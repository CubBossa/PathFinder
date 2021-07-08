package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.hooks.CitizensUtils;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class QuestFindable extends NpcFindable {

    public static final String SCOPE = "QUEST";

    public QuestFindable(int databaseId, RoadMap roadMap, int npcId) {
        super(databaseId, roadMap, CitizensUtils.getNPC(npcId));
    }

    @Override
    public String getScope() {
        return "QUEST";
    }

    @Override
    void updateData() {

    }
}
