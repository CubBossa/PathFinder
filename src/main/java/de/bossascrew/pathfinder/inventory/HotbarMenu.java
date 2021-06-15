package de.bossascrew.pathfinder.inventory;

import de.bossascrew.core.functional.Consumer3;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    private final Map<UUID, ItemStack[]> playerItems;
    private final ItemStack[] specialItems;
    //private final Consumer3<Integer, Player, Block>[] clickHandlersBlocks;


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
        //clickHandlersBlocks = (BiConsumer<Integer, Player>[]) new BiConsumer[9];
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }
}
