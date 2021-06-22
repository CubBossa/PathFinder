package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.DtlTraderNode;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;

@Getter
public class Shop {

    private final String identifier;
    private final String name;
    private DtlTraderNode traderNode;
    private final Map<ItemStack, String> itemStackPermissionMap;

    public Shop(File file) {
        this.identifier = "";
        this.name = "";
        this.itemStackPermissionMap = loadItemStacksAndPermission();
    }

    private Map<ItemStack, String> loadItemStacksAndPermission() {
        return null;
    }
}
