package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

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
        File dirShops = new File(getPlugin().getDataFolder().getParent(),"dtlTradersPlus/shops/");
        File npcs = new File(getPlugin().getDataFolder().getParent(),"Citizens/saves.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(npcs);

        Map<String, Integer> npcShopMap = new HashMap<>();
        ConfigurationSection npcSection = cfg.getConfigurationSection("npc");
        for(String key : npcSection.getKeys(false)) {
            ConfigurationSection certainNpcSection = npcSection.getConfigurationSection(key);
            int id = Integer.parseInt(key);
            ConfigurationSection traitsSection = certainNpcSection.getConfigurationSection("traits");
            if(traitsSection.isConfigurationSection("trader")) {
                ConfigurationSection traderSection = traitsSection.getConfigurationSection("trader");
                String guiName = traderSection.getString("guiName").toLowerCase();
                npcShopMap.put(guiName, id);
            }
        }

        for(File file : dirShops.listFiles()) {
            System.out.println("Lade Shop aus File: " + file.getName());
            File subFile = Arrays.stream(file.listFiles()).filter(f -> f.getName().equals(file.getName() + ".yml")).findFirst().orElse(null);
            if(subFile == null) {
                PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Laden eines Files: " + file.getName());
                continue;
            }
            Integer id = npcShopMap.get(file.getName());
            if(id != null) {
                shops.add(new Shop(id, subFile));
            } else {
                PathPlugin.getInstance().getLogger().log(Level.SEVERE, "NPC-ID in Shop-Mapping nicht gefunden: " + file.getName());
            }
        }
    }
}
