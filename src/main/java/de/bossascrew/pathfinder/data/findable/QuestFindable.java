package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class QuestFindable extends NpcFindable {

    public QuestFindable(int databaseId, RoadMap roadMap, NPC npc) {
        super(databaseId, roadMap, npc);
        this.npc = npc;
    }

    @Override
    void updateData() {

    }
}
