package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import org.bukkit.inventory.ItemStack;

public class ChestShopHook extends Hook {

    //ChestShopLogger 0.3.0 als dependency einbauen

    public ChestShopHook(PathPlugin plugin) {
        super(plugin);
    }

    public void getShops(ItemStack searchedItem) {
        //TODO return Shop
    }
}
