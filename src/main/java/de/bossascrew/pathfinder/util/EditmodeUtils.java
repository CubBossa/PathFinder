package de.bossascrew.pathfinder.util;

import de.bossascrew.core.bukkit.util.HeadDBUtils;
import de.bossascrew.core.bukkit.util.ItemStackUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class EditmodeUtils {

    public static final ItemStack NODE_TOOl = createTool(Material.NETHER_STAR, ChatColor.WHITE + "Wegpunkte-Tool",
            ChatColor.GRAY + "Rechtsklick: Node erstellen\n" + ChatColor.GRAY + "Linksklick: Node löschen");
    public static final ItemStack EDGE_TOOL = createTool(Material.STICK, ChatColor.WHITE + "Kanten-Tool",
            ChatColor.GRAY + "Rechtsklick auf Nodes: Kante aufspannen\n" + ChatColor.GRAY + "Linksklick auf Node: Alle Kanten löschen" +
                    "\nLinksklick auf Kante: Kante löschen");
    public static final ItemStack GROUP_TOOl = createTool(Material.CHEST, ChatColor.WHITE + "Wegpunktgruppe",
            ChatColor.GRAY + "Rechtsklick: öffnet Gruppen-GUI\n" + ChatColor.GRAY + "Linksklick: Resettet Gruppe");
    public static final ItemStack TANGENT_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Tangentenwichtung",
            ChatColor.GRAY + "Rechtsklick: Tangentenwichtung setzen\n" + ChatColor.GRAY + "Linksklick: auf Standard setzen");
    public static final ItemStack PERMISSION_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Permission",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\n" + ChatColor.GRAY + "Linksklick: zurücksetzen");
    public static final ItemStack RENAME_TOOL = createTool(Material.NAME_TAG, ChatColor.WHITE + "Umbenennen",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\n" + ChatColor.GRAY + "Linksklick: zurücksetzen");
    public static final ItemStack TP_TOOL = createTool(Material.ENDER_PEARL, ChatColor.WHITE + "Teleport",
            ChatColor.GRAY + "Rechtsklick: zu nächster Node\n" + ChatColor.GRAY + "Linksklick: zur zuletzt erstellten Node");

    private static ItemStack createTool(Material type, String name, String lore) {
        ItemStack item = new ItemStack(type);
        ItemStackUtils.setNameAndLore(item, name, lore);
        return item;
    }

    public static ArmorStand getNewArmorStand(Location location, String name, int headDbId) {
        return getNewArmorStand(location, name, headDbId, false);
    }

    public static ArmorStand getNewArmorStand(Location location, String name, int headDbId, boolean small) {
        ArmorStand as = location.getWorld().spawn(location,
                ArmorStand.class,
                armorStand -> {
                    armorStand.setVisible(false);
                    if (name != null) {
                        armorStand.setCustomNameVisible(true);
                        armorStand.setCustomName(name);
                    }
                    armorStand.setGravity(false);
                    armorStand.setInvulnerable(true);
                    armorStand.setSmall(small);
                    ItemStack helmet = HeadDBUtils.getHeadById(headDbId);
                    if (armorStand.getEquipment() != null && helmet != null) {
                        armorStand.getEquipment().setHelmet(helmet);
                    }
                });
        return as;
    }
}
