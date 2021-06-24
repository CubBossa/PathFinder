package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class DtlTraderNode extends Node implements NpcFindable {

    private final NPC npc;

    public DtlTraderNode(int databaseId, RoadMap roadMap, String name, Vector vector, NPC npc) {
        super(databaseId, roadMap, name, vector);
        this.npc = npc;
    }

    public int getNpcId() {
        return npc.getId();
    }

    public NPC getNpc() {
        return npc;
    }

    public Location getLocation() {
        return npc.getStoredLocation();
    }

    public Vector getVector() {
        return getLocation().toVector();
    }

    public String getName() {
        return npc.getFullName();
    }
}
