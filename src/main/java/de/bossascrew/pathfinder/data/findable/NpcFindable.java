package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.StringUtils;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class NpcFindable extends Node {

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
        return getNpc().getEntity().getLocation().clone().add(0, 0.5, 0);
    }

    public Vector getVector() {
        return getLocation().toVector();
    }

    @Override
    public String getNameFormat() {
        return nameFormat == null ? formatName(getNpc().getFullName()) : nameFormat;
    }

    public static String formatName(String name) {
        List<Integer> paragraphs = new ArrayList<>();
        int index = 0;
        for (char c : name.toCharArray()) {
            if (c == '§') {
                paragraphs.add(index);
            }
            index++;
        }
        String ret = name;
        for (int count = 0; count < paragraphs.size(); count++) {
            int subtract = paragraphs.get(count) - count * 2;
            try {
                ret = ret.substring(0, subtract) + ret.substring(subtract + 2);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        }
        return StringUtils.replaceBlanks(ret);
    }
}
