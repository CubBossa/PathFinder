package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.Messages;
import de.cubbossa.translations.TranslatedItem;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class EditmodeUtils {

    public static final ItemStack NODE_TOOL = new TranslatedItem(Material.NETHER_STAR, Messages.E_NODE_TOOL_N, Messages.E_NODE_TOOL_L).createItem();
    public static final ItemStack EDGE_TOOL = new TranslatedItem(Material.STICK, Messages.E_EDGE_TOOL_N, Messages.E_EDGE_TOOL_L).createItem();
    public static final ItemStack EDGE_TOOL_GLOW = new TranslatedItem(Material.STICK, Messages.E_EDGE_TOOL_N, Messages.E_EDGE_TOOL_L).createItem();

    static {
        ItemStackUtils.setGlow(EDGE_TOOL_GLOW);
    }

    public static final ItemStack GROUP_TOOL = new TranslatedItem(Material.CHEST, Messages.E_GROUP_TOOL_N, Messages.E_GROUP_TOOL_L).createItem();
    public static final ItemStack LAST_GROUP_TOOL = new TranslatedItem(Material.ENDER_CHEST, Messages.E_LAST_GROUP_TOOL_N, Messages.E_LAST_GROUP_TOOL_L).createItem();
    public static final ItemStack CURVE_TOOL = new TranslatedItem(Material.LEAD, Messages.E_CURVE_TOOL_N, Messages.E_CURVE_TOOL_L).createItem();
    public static final ItemStack PERMISSION_TOOL = new TranslatedItem(Material.STRUCTURE_VOID, Messages.E_PERM_TOOL_N, Messages.E_PERM_TOOL_L).createItem();
    public static final ItemStack TP_TOOL = new TranslatedItem(Material.ENDER_PEARL, Messages.E_TP_TOOL_N, Messages.E_TP_TOOL_L).createItem();
}
