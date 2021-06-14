package de.bossascrew.pathfinder.data.findable;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Quester extends Node implements NpcFindable {

    private final NPC npc;

    public Quester(int databaseId, int roadMapId, String name, Vector vector, NPC npc) {
        super(databaseId, roadMapId, name, vector);
        this.npc = npc;
    }

    public int getNpcId() {
        return npc.getId();
    }

    public NPC getNpc() {
        return npc;
    }

    public Location getLocation() {
        return npc.getEntity().getLocation();
    }

    public Vector getVector() {
        return getLocation().toVector();
    }

    public String getName() {
        return npc.getFullName();
    }
}
