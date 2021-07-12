package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import de.bossascrew.pathfinder.util.hooks.CitizensUtils;
import lombok.Getter;
import lombok.Setter;

public class TraderFindable extends NpcFindable {

    public static final String SCOPE = "TRADER";

    @Getter
    @Setter
    private Shop shop;

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
