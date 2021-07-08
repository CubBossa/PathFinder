package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.hooks.CitizensUtils;
import net.citizensnpcs.api.npc.NPC;

public class TraderFindable extends NpcFindable {

    public static final String SCOPE = "TRADER";

    public TraderFindable(int databaseId, RoadMap roadMap, int npcId) {
        super(databaseId, roadMap, CitizensUtils.getNPC(npcId));
    }

    @Override
    public String getScope() {
        return "TRADER";
    }

    @Override
    void updateData() {

    }
}
