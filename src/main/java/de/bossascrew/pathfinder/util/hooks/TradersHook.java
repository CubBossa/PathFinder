package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.Shop;

import java.util.ArrayList;
import java.util.List;

public class TradersHook extends Hook {

    List<Shop> shops;

    public TradersHook(PathPlugin plugin) {
        super(plugin);
        shops = new ArrayList<>();
    }

    public void loadShopsFromDir() {



    }
}
