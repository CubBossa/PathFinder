package de.bossascrew.pathfinder.data;

import com.google.common.collect.Maps;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;

public class Shop {

    private final String identifier;
    private final String name;
    private final Map<ItemStack, String> itemStackPermissionMap;

    public Shop(File file) {
        this.identifier = "";
        this.name = "";
        this.itemStackPermissionMap = Maps.newHashMap();
    }
}
