package de.bossascrew.pathfinder.data;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import de.bossascrew.core.util.PluginUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Shop {

    @RequiredArgsConstructor
    @Getter
    public static class ShopItem {
        private final ItemStack itemStack;
        private final String permission;
        private final double price;
    }

    private final int npcId;
    private final String identifier;
    private final String name;
    private final Map<ItemStack, ShopItem> buyItemMap;
    private final Map<ItemStack, ShopItem> sellItemMap;

    public Shop(int npcId, File file) {
        this.npcId = npcId;
        this.identifier = "";
        this.name = "";
        this.buyItemMap = new HashMap<>();
        this.sellItemMap = new HashMap<>();
        PluginUtils.getInstance().runAsync(() -> loadItemStacksAndPermission(file));
    }

    private void loadItemStacksAndPermission(File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection pages = cfg.getDefaultSection().getConfigurationSection("pages");
        for(String pageKey : pages.getKeys(false)) {
            ConfigurationSection page = pages.getConfigurationSection(pageKey);
            ConfigurationSection buyItems = page.getConfigurationSection("buy-items");
            for(String itemKey : buyItems.getKeys(false)) {
                ConfigurationSection item = buyItems.getConfigurationSection(itemKey);
                ItemStack stack = item.getItemStack("item");
                String permission = item.getString("permission");
                double price = item.getDouble("trade-price");
                buyItemMap.put(stack, new ShopItem(stack, permission, price));
            }
            ConfigurationSection sellItems = page.getConfigurationSection("sell-items");
            for(String itemKey : sellItems.getKeys(false)) {
                ConfigurationSection item = sellItems.getConfigurationSection(itemKey);
                ItemStack stack = item.getItemStack("item");
                String permission = item.getString("permission");
                double price = item.getDouble("trade-price");
                sellItemMap.put(stack, new ShopItem(stack, permission, price));
            }
        }
    }
}
