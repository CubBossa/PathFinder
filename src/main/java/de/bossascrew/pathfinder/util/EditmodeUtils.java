package de.bossascrew.pathfinder.util;

import de.bossascrew.core.bukkit.util.ItemStackUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class EditmodeUtils {

    public static final ItemStack NODE_TOOl = createTool(Material.NETHER_STAR, ChatColor.WHITE + "Wegpunkte-Tool",
            ChatColor.GRAY + "Rechtsklick: Node erstellen\n" +
                    ChatColor.GRAY + "Linksklick: Node löschen");
    public static final ItemStack EDGE_TOOL = createTool(Material.STICK, ChatColor.WHITE + "Kanten-Tool",
            ChatColor.GRAY + "Rechtsklick auf Nodes: Kante aufspannen\n" +
                    ChatColor.GRAY + "Linksklick auf Node: Alle Kanten löschen\n" +
                    ChatColor.GRAY + "Linksklick auf Kante: Kante löschen\n" +
                    ChatColor.GRAY + "Linksklick in Luft: Verbinden abbrechen");
    public static final ItemStack EDGE_TOOL_GLOW = EDGE_TOOL.clone();

    static {
        ItemStackUtils.setGlowing(EDGE_TOOL_GLOW);
    }

    public static final ItemStack GROUP_TOOL = createTool(Material.CHEST, ChatColor.WHITE + "Wegpunktgruppe",
            ChatColor.GRAY + "Rechtsklick: Öffnet Gruppen-Menü\n" +
                    ChatColor.GRAY + "Linksklick: Resettet Gruppe");
    public static final ItemStack LAST_GROUP_TOOL = createTool(Material.TRAPPED_CHEST, ChatColor.WHITE + "Letzte Weggruppe",
            ChatColor.GRAY + "Rechtsklick: Setzt letzte Weggruppe");
    public static final ItemStack TANGENT_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Tangentenwichtung",
            ChatColor.GRAY + "Rechtsklick: Tangentenwichtung setzen\n" +
                    ChatColor.GRAY + "Linksklick: auf Standard setzen");
    public static final ItemStack PERMISSION_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Permission",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\n" +
                    ChatColor.GRAY + "Linksklick: zurücksetzen");
    public static final ItemStack RENAME_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Umbenennen",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\n" +
                    ChatColor.GRAY + "Linksklick: zurücksetzen");
    public static final ItemStack TP_TOOL = createTool(Material.ENDER_PEARL, ChatColor.WHITE + "Teleport",
            ChatColor.GRAY + "Rechtsklick: zu nächster Node\n" +
                    ChatColor.GRAY + "Linksklick: zur zuletzt erstellten Node");

    private static ItemStack createTool(Material type, String name, String lore) {
        ItemStack item = new ItemStack(type);
        ItemStackUtils.setNameAndLore(item, name, lore);
        return item;
    }
}
