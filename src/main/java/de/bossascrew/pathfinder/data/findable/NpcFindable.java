package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
public abstract class NpcFindable extends Findable {

    protected final int id;
    protected NPC npc = null;

    public NpcFindable(int databaseId, RoadMap roadMap, int id, String name) {
        super(databaseId, roadMap, name);
        this.id = id;
    }

    public int getNpcId() {
        return id;
    }

    public NPC getNpc() {
        return npc == null ? CitizensAPI.getNPCRegistry().getById(id) : npc;
    }

    public Location getLocation() {
        return getNpc().getEntity().getLocation();
    }

    public Vector getVector() {
        return getLocation().toVector();
    }

    public String getFinalName() {
        return name == null ? getNpc().getFullName() : name;
    }
}
