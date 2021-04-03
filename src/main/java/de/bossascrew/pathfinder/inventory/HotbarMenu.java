package de.bossascrew.pathfinder.inventory;

import com.google.common.base.Preconditions;
import de.bossascrew.core.functional.Consumer3;
import de.bossascrew.pathfinder.PathPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * GUI bei dem nur die 9 Items in der Hotbar belegt werden und durch Interagieren getriggert werden
 */
public class HotbarMenu {

    public interface BlockClickHandler extends Consumer3<Integer, Player, Block> {

    }

    public interface EntityClickHandler extends Consumer3<Integer, Player, Entity> {

    }

    @Getter
    @Setter
    private @Nullable
    Consumer<Player> closeHandler;

    @Getter
    private final @Nullable
    BiConsumer<Integer, Player> defaultClickHandler;

    @Getter
    private final boolean allowPlayerModifyInventory;

    @Getter
    @Setter
    private boolean lockBeforeClickHandler = true;

    @Getter
    private boolean locked;

    private Map<UUID, ItemStack[]> playerItems;
    private final ItemStack[] specialItems;
    private final Consumer3<Integer, Player, Block>[] clickHandlersBlocks;


    public HotbarMenu() {
        this(false);
    }

    public HotbarMenu(boolean allowPlayerModifyInventory) {
        this(null, null, allowPlayerModifyInventory);
    }

    public HotbarMenu(@Nullable BiConsumer<Integer, Player> defaultClickHandler,
                   @Nullable Consumer<Player> closeHandler) {
        this(defaultClickHandler, closeHandler, false);
    }

    @SuppressWarnings("unchecked")
    public HotbarMenu(@Nullable BiConsumer<Integer, Player> defaultClickHandler,
                   @Nullable Consumer<Player> closeHandler,
                   boolean allowPlayerModifyInventory) {

        this.defaultClickHandler = defaultClickHandler;
        this.allowPlayerModifyInventory = allowPlayerModifyInventory;

        playerItems = new HashMap<UUID, ItemStack[]>();
        specialItems = new ItemStack[9];
        clickHandlersBlocks = (BiConsumer<Integer, Player>[]) new BiConsumer[9];
    }


    private void checkIndex(int index) {
        Preconditions.checkArgument(index >= 0, "index muss >= 0 sein");
        Preconditions.checkArgument(index < 9, "index muss < 9 sein");
    }

    public @Nullable
    ItemStack getSpecialItem(int slot) {
        checkIndex(slot);

        return specialItems[slot];
    }

    public @Nullable
    BiConsumer<Integer, Player> getClickHandler(int slot) {
        checkIndex(slot);

        return clickHandlersBlocks[slot];
    }

    public void setSpecialItemAndClickHandler(int index, @Nullable ItemStack specialItem, @Nullable BiConsumer<Integer, Player> clickHandler) {
        checkIndex(index);

        specialItems[index] = specialItem;
        clickHandlersBlocks[index] = clickHandler;
    }

    public void openInventory(Player player) {
        if (player.isSleeping()) {
            player.wakeup(true);
        }

        ItemStack[] playerHotbar = new ItemStack[9];
        playerItems.put(player.getUniqueId(), playerHotbar);


        for (int index = 0; index < 9; index++) {
            ItemStack specialItem = specialItems[index];
            if (specialItem == null) {
                player.getInventory().setItem(index, null);
            }
            player.getInventory().setItem(index, specialItem.clone());
        }

        UUID playerId = player.getUniqueId();
        HotbarMenuHandler.getInstance().getOpenMenus().put(playerId, this);
    }

    public boolean handleInventoryClick(Player player, InventoryClickEvent event) {
        int index = event.getRawSlot();
        if (index < 0 || index >= 9) {
            return false;
        }

        if (locked) {
            event.setCancelled(true);

            return false;
        }

        BiConsumer<Integer, Player> clickHandler = clickHandlersBlocks[index];
        if (clickHandler == null) {
            clickHandler = defaultClickHandler;
        }

        if (clickHandler == null) {
            if (!allowPlayerModifyInventory || getSpecialItem(index) != null) {
                // Klicks innerhalb des virtuellen Inventars abbrechen
                event.setCancelled(true);
            }

            return false;
        }

        if (lockBeforeClickHandler) {
            lock();
        }

        try {
            clickHandler.accept(index, player);
        } catch (Exception exc) {
            PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Fehler bei handleInventoryClick() von Spieler " + player.getName(), exc);
        }
        event.setCancelled(true);
        return true;
    }

    public boolean handleInventoryClose(Player player) {

        ItemStack[] hotbar = playerItems.get(player.getUniqueId());
        for (int index = 0; index < 9; index++) {
            ItemStack item = hotbar[index];
            player.getInventory().setItem(index, item);
        }
        if (closeHandler == null) {
            return false;
        }

        try {
            closeHandler.accept(player);
        } catch (Exception exc) {
            PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Fehler bei handleInventoryClose() von Spieler " + player.getName(), exc);
        }
        return true;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }
}
