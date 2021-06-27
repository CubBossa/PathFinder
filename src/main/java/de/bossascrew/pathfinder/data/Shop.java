package de.bossascrew.pathfinder.data;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;

@Getter
public class Shop {

    private final int npcId;
    private final String identifier;
    private final String name;
    private final Map<ItemStack, String> itemStackPermissionMap;

    public Shop(int npcId, File file) {
        this.npcId = npcId;
        this.identifier = "";
        this.name = "";
        this.itemStackPermissionMap = loadItemStacksAndPermission();
    }

    private Map<ItemStack, String> loadItemStacksAndPermission() {
        return null;
    }
}
