package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.Shop;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TradersHook extends Hook {

    @Getter
    private static TradersHook instance;

    @Getter
    private final List<Shop> shops;

    public TradersHook(PathPlugin plugin) {
        super(plugin);
        shops = new ArrayList<>();
        instance = this;
    }

    public void loadShopsFromDir() {
        File dir = new File(getPlugin().getDataFolder().getParent(),"dtlTradersPlus/shops/");
        for(File file : dir.listFiles()) {
            System.out.println("Lade Shop aus File: " + file.getName());
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
