package de.bossascrew.pathfinder.util.hooks;

import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class ChestShopHook extends Hook {

    @Getter
    private static ChestShopHook instance;

    //ChestShopLogger 0.3.0 als dependency einbauen

    public ChestShopHook(PathPlugin plugin) {
        super(plugin);
        instance = this;
    }

    public void getShops(ItemStack searchedItem) {
        //TODO return Shop
    }
}
