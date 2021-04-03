package de.bossascrew.pathfinder.inventory;

import com.google.common.collect.Maps;
import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;

public class HotbarMenuHandler {

    private PathPlugin plugin;

    @Getter
    private static HotbarMenuHandler instance;

    @Getter
    private final Map<UUID, HotbarMenu> openMenus = Maps.newHashMap();

    public HotbarMenuHandler(PathPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public boolean handleInventoryClick(Player player, InventoryClickEvent event) {
        UUID playerId = player.getUniqueId();
        HotbarMenu invMenu = openMenus.get(playerId);
        if (invMenu == null) {
            return false;
        }

        return invMenu.handleInventoryClick(player, event);
    }

    public boolean handleInventoryClose(Player player) {
        UUID playerId = player.getUniqueId();
        HotbarMenu invMenu = openMenus.remove(playerId);
        if (invMenu == null) {
            return false;
        }

        return invMenu.handleInventoryClose(player);
    }
}
