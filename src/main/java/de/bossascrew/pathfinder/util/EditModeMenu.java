package de.bossascrew.pathfinder.util;

import de.bossascrew.core.bukkit.inventory.menu.AnvilMenu;
import de.bossascrew.core.bukkit.inventory.menu.HotbarAction;
import de.bossascrew.core.bukkit.inventory.menu.HotbarMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.bukkit.util.HeadDBUtils;
import de.bossascrew.core.bukkit.util.ItemStackUtils;
import de.bossascrew.core.util.CommandUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class EditModeMenu {

    private final Player player;
    private final RoadMap roadMap;

    @Getter
    @Setter
    private Findable firstFindableEdgeCreate = null;

    public HotbarMenu getHotbarMenu() {
        HotbarMenu menu = new HotbarMenu();

        menu.setDefaultClickHandler(HotbarAction.DROP_ITEM, context -> {
            Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> roadMap.setEditMode(player.getUniqueId(), false), 1L);
        });

        menu.setItem(0, EditmodeUtils.NODE_TOOl);
        menu.setClickHandler(0, HotbarAction.LEFT_CLICK_ENTITY, context -> {
            Findable clickedFindable = getClickedFindable((Entity) context.getTarget());
            if (clickedFindable != null) {
                Player p = context.getPlayer();
                roadMap.deleteFindable(clickedFindable);
                p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
            }
        });
        menu.setClickHandler(0, HotbarAction.RIGHT_CLICK_BLOCK, context ->
                openNodeNameMenu(context.getPlayer(), ((Block) context.getTarget()).getLocation().toVector().add(new Vector(0.5, 1.5, 0.5))));

        menu.setItem(1, EditmodeUtils.EDGE_TOOL);
        menu.setClickHandler(1, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Player p = context.getPlayer();
            Findable clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked == null) {
                return;
            }
            if (this.firstFindableEdgeCreate == null) {
                firstFindableEdgeCreate = clicked;
                PlayerUtils.sendMessage(p, PathPlugin.PREFIX + "Klicke einen weiteren Wegpunkt zum Verbinden.");
            } else {
                if (firstFindableEdgeCreate.equals(clicked)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                roadMap.connectNodes(firstFindableEdgeCreate, clicked);
                firstFindableEdgeCreate = null;
                p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1, 1);
            }
        });
        //TODO leftclick

        menu.setItem(2, EditmodeUtils.TP_TOOL);
        menu.setClickHandler(2, new HotbarAction[]{HotbarAction.RIGHT_CLICK_ENTITY, HotbarAction.RIGHT_CLICK_BLOCK, HotbarAction.RIGHT_CLICK_AIR}, context -> {
            double dist = -1;
            Findable nearest = null;
            for (Findable findable : roadMap.getFindables()) {
                double _dist = findable.getVector().distance(player.getLocation().toVector());
                if (dist == -1 || _dist < dist) {
                    nearest = findable;
                    dist = _dist;
                }
            }
            if (nearest == null) {
                return;
            }
            Player p = context.getPlayer();
            Location newLoc = nearest.getLocation().setDirection(p.getLocation().getDirection());
            p.teleport(newLoc);
            p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1, 1);
        });

        menu.setItemAndClickHandler(3, EditmodeUtils.TANGENT_TOOL, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Findable clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked != null) {
                openTangentStrengthMenu(context.getPlayer(), clicked);
            }
        });

        menu.setItemAndClickHandler(4, EditmodeUtils.PERMISSION_TOOL, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Findable clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked != null) {
                openNodePermissionMenu(context.getPlayer(), clicked);
            }
        });

        menu.setItem(6, EditmodeUtils.GROUP_TOOl);
        menu.setClickHandler(6, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            //TODO chestmenu öffnen?
        });
        //Kiste: GruppenGUI: erstes item barriere = keine gruppe. dann alle gruppen als nametags. unten rechts emerald für neue gruppe.
        //rechtsklick auf gruppe = zuweisen. Linksklick mit Confirm = gruppe löschen.
        //Gruppenicons haben in der Lore eine Liste aller Nodes, die Teil der Gruppe sind.
        return menu;
    }

    private @Nullable
    Findable getClickedFindable(Entity entity) {
        Findable clickedFindable = null;
        for (Findable f : roadMap.getEditModeNodeArmorStands().keySet()) {
            Entity e = roadMap.getEditModeNodeArmorStands().get(f);
            if (e.equals(entity)) {
                clickedFindable = f;
                break;
            }
        }
        return clickedFindable;
    }

    private void openNodeNameMenu(Player player, Vector position) {
        AnvilMenu menu = new AnvilMenu(Component.text("Node erstellen:"));

        ItemStack result = HeadDBUtils.getHeadById(roadMap.getEditModeVisualizer().getNodeHeadId());
        ItemStack info = result.clone();
        ItemStackUtils.setNameAndLore(info, ChatColor.WHITE + "Wegpunkt erstellen",
                CommandUtils.wordWrap(ChatColor.GRAY + "Gib einen für diese Straßenkarte einzigartigen Namen an.", "\n" + ChatColor.GRAY, 30));
        ItemStack error = ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.RED + "Ungültige Eingabe", ChatColor.GRAY + "Name vergeben.");

        menu.setItem(0, info);
        menu.setTextInputHandler((player1, s) -> {
            if (!roadMap.isNodeNameUnique(menu.getTextBoxText())) {
                menu.setItem(2, error);
            } else {
                ItemStackUtils.setNameAndLore(result, s, "");
                menu.setItem(2, result);
            }
        });
        menu.setItemAndClickHandler(2, result, context -> {
            Player p = context.getPlayer();
            if (!roadMap.isNodeNameUnique(menu.getTextBoxText())) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            roadMap.createNode(position, menu.getTextBoxText());
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            menu.closeInventory();
        });
        menu.open(player);
    }

    private void openTangentStrengthMenu(Player player, Findable findable) {
        AnvilMenu menu = new AnvilMenu(Component.text("Rundung einstellen:"));

        ItemStack result = HeadDBUtils.getHeadById(roadMap.getEditModeVisualizer().getNodeHeadId());
        ItemStack info = result.clone();
        ItemStackUtils.setNameAndLore(info, ChatColor.WHITE + "Tangentenstärke setzen",
                CommandUtils.wordWrap(ChatColor.GRAY + "Mit der Tangentenstärke setzt man, wie weit die Kontrollpunkte von dem Wegpunkt entfernt sein " +
                                "sollen und damit, wie sehr der Pfad gerundet werden soll. Die Eingaben none oder null setzen den Wert auf den Straßenkarten-Standard.",
                        "\n" + ChatColor.GRAY, 35));
        ItemStack error = ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.RED + "Ungültige Eingabe", ChatColor.GRAY + "Beispiel: 7.3");

        menu.setItem(0, info);
        menu.setTextInputHandler((player1, s) -> {
            if (s == null) {
                menu.setItem(2, error);
                return;
            }
            if (s.equalsIgnoreCase("null") || s.equalsIgnoreCase("none")) {
                ItemStackUtils.setNameAndLore(result, ChatColor.WHITE + "Keine", "");
                menu.setItem(2, result);
                return;
            }
            double val = 0;
            try {
                val = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                menu.setItem(2, error);
                return;
            }
            ItemStackUtils.setNameAndLore(result, ChatColor.WHITE + "" + val + " Blöcke", "");
            menu.setItem(2, result);
        });
        menu.setItemAndClickHandler(2, result, context -> {
            String in = menu.getTextBoxText();
            if (in == null) {
                return;
            }
            Player p = context.getPlayer();
            Double val = null;
            if (in.equalsIgnoreCase("null") || in.equalsIgnoreCase("none")) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                findable.setBezierTangentLength(val);
                menu.closeInventory();
                return;
            }
            try {
                val = Double.parseDouble(in);
            } catch (NumberFormatException e) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            findable.setBezierTangentLength(val);
            menu.closeInventory();
        });
        menu.open(player);
    }

    private void openNodePermissionMenu(Player player, Findable findable) {
        AnvilMenu menu = new AnvilMenu(Component.text("Permission setzen:"));

        ItemStack result = HeadDBUtils.getHeadById(roadMap.getEditModeVisualizer().getNodeHeadId());
        ItemStack info = result.clone();
        ItemStackUtils.setNameAndLore(info, ChatColor.WHITE + "Permission setzen", CommandUtils.wordWrap(ChatColor.GRAY +
                        "Hier kann bestimmt werden, welche Nodes ein Spieler finden darf und welche nicht. Die Eingaben null und none deaktivieren die Permissionabfrage",
                "\n" + ChatColor.GRAY, 35));
        ItemStackUtils.setNameAndLore(result, PathPlugin.CHAT_COLOR_LIGHT + "Bestätigen", "");

        menu.setItem(0, info);
        menu.setTextInputHandler((player1, s) -> {
            menu.setItem(2, result);
        });
        menu.setItemAndClickHandler(2, result, context -> {
            String in = menu.getTextBoxText();
            if (in == null) {
                return;
            }
            Player p = context.getPlayer();
            findable.setPermission(in.equalsIgnoreCase("null") || in.equalsIgnoreCase("none") ? null : in);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            menu.closeInventory();
        });
        menu.open(player);
    }
}
