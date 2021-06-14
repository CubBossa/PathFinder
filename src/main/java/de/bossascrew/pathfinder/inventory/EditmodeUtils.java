package de.bossascrew.pathfinder.inventory;

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

    public static final ItemStack NODE_TOOl = createTool(Material.NETHER_STAR, "§f§nWegpunkte-Tool",
            ChatColor.GRAY + "Rechtsklick: Node erstellen\nLinksklick: Node löschen");
    public static final ItemStack EDGE_TOOL = createTool(Material.STICK, "§f§nKanten-Tool",
            ChatColor.GRAY + "Rechtsklick auf Nodes: Kante aufspannen\nLinksklick auf Node: Alle Kanten löschen" +
                    "\nLinksklick auf Kante: Kante löschen");
    public static final ItemStack GROUP_TOOl = createTool(Material.CHEST, "§f§nWegpunktgruppe",
            ChatColor.GRAY + "Rechtsklick: öffnet Gruppen-GUI\nLinksklick: Resettet Gruppe");
    public static final ItemStack TANGENT_TOOL = createTool(Material.SLIME_BALL, "§f§nTangentenwichtung",
            ChatColor.GRAY + "Rechtsklick: Tangentenwichtung setzen\nLinksklick: auf Standard setzen");
    public static final ItemStack PERMISSION_TOOL = createTool(Material.NAME_TAG, "§f§nPermission",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\nLinksklick: zurücksetzen");
    public static final ItemStack RENAME_TOOL = createTool(Material.NAME_TAG, "§f§nUmbenennen",
            ChatColor.GRAY + "Rechtsklick: Permission-Node setzen\nLinksklick: zurücksetzen");
    public static final ItemStack TP_TOOL = createTool(Material.NAME_TAG, "§f§nSchnelltp",
            ChatColor.GRAY + "Rechtsklick: zu nächster Node\nLinksklick: zur zuletzt erstellten Node");

    private static ItemStack createTool(Material type, String name, String lore) {
        ItemStack item = new ItemStack(type);
        ItemStackUtils.setNameAndLore(item, name, lore);
        return item;
    }

    public static ArmorStand getNewArmorStand(Location location, String name, int headDbId) {
        ArmorStand as = (ArmorStand) location.getWorld().spawn(location,
                ArmorStand.class,
                armorStand -> {
                    armorStand.setVisible(false);
                    if (name != null) {
                        armorStand.setCustomNameVisible(true);
                        armorStand.setCustomName(name);
                    }
                    armorStand.setGravity(false);
                    armorStand.setInvulnerable(true);
                    ItemStack helmet = HeadDBUtils.getHeadById(headDbId);
                    if (armorStand.getEquipment() != null && helmet != null) {
                        armorStand.getEquipment().setHelmet(helmet);
                    }
                });
        return as;
    }

    public static HotbarMenu getNewMenu() {
        HotbarMenu menu = new HotbarMenu();

        //menu.setSpecialItemAndClickHandler(0, EditmodeUtils.EDGE_TOOL, (integer, whoClicked) -> {

        //TODO

        //});


        //TODO equippe mit wichtigen Items für Editmode

        //wegpunkt werkzeug: rechtsklick setzen, linksklick löschen
        //kantenwerkzeug: rechtsklick kante aufspannen, linksklick alle kanten eines nodes löschen. Linksklick auf kante = löschen
        //kompass: tp zum nächsten Node
        //Slimeball: Rundung der Tangenten einstellen
        //nametag: Permissionnode setzen
        //Kiste: GruppenGUI: erstes item barriere = keine gruppe. dann alle gruppen als nametags. unten rechts emerald für neue gruppe.
        //rechtsklick auf gruppe = zuweisen. Linksklick mit Confirm = gruppe löschen.
        //Gruppenicons haben in der Lore eine Liste aller Nodes, die Teil der Gruppe sind.

        return menu;
    }
}