package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
public abstract class NpcFindable extends Findable {

    NPC npc;

    public NpcFindable(int databaseId, RoadMap roadMap, NPC npc) {
        super(databaseId, roadMap);
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
