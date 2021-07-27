package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import lombok.Getter;
import lombok.Setter;

public class TraderFindable extends NpcFindable {

    public static final String SCOPE = "TRADER";

    @Getter
    @Setter
    private Shop shop;

    public TraderFindable(int databaseId, RoadMap roadMap, int npcId, String name) {
        super(databaseId, roadMap, npcId, name);
    }

    @Override
    public String getScope() {
        return SCOPE;
    }

    @Override
    void updateData() {

    }
}
