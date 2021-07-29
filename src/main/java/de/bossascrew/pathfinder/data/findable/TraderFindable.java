package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import de.bossascrew.pathfinder.util.hooks.TradersHook;
import lombok.Getter;
import lombok.Setter;

public class TraderFindable extends NpcFindable {

    public static final String SCOPE = "TRADER";

    @Getter
    @Setter
    private Shop shop;

    public TraderFindable(int databaseId, RoadMap roadMap, int npcId, String name) {
        super(databaseId, roadMap, npcId, name);
        setShop(TradersHook.getInstance().getShops().stream().filter(s -> s.getNpcId() == id).findAny().orElse(null));
        System.out.println("Setze Shop: " + shop);
    }

    @Override
    public String getName() {
        return shop.getName();
    }

    @Override
    public String getScope() {
        return SCOPE;
    }

    @Override
    void updateData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updateFindable(this));
    }
}
