package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.Shop;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TradersHook extends Hook {

    List<Shop> shops;

    public TradersHook(PathPlugin plugin) {
        super(plugin);
        shops = new ArrayList<>();
    }

    public void loadShopsFromDir() {
        for(File file : new File("asd").listFiles()) { //TODO
            NPC trader = null;
            for(NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
                //TODO wenn npc trait trader mit filenamen hat, zwischenspeichern
            }
            if(trader == null) {
                continue;
            }
            shops.add(new Shop(trader.getId(), file));
        }
    }
}
