package de.bossascrew.pathfinder.util;

import de.bossascrew.core.bukkit.inventory.menu.*;
import de.bossascrew.core.bukkit.util.HeadDBUtils;
import de.bossascrew.core.bukkit.util.ItemStackUtils;
import de.bossascrew.core.util.CommandUtils;
import de.bossascrew.core.util.Pair;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.NodeGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.node.NpcFindable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.util.hooks.CitizensHook;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditModeMenu {

    private final Player player;
    private final GameMode playerGameMode;
    private final RoadMap roadMap;
    private final PathPlayer pathPlayer;

    @Getter
    @Setter
    private Waypoint firstFindableEdgeCreate = null;

    public EditModeMenu(Player player, RoadMap roadMap) {
        this.player = player;
        this.playerGameMode = player.getGameMode();
        this.roadMap = roadMap;
        this.pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
    }

    public HotbarMenu getHotbarMenu() {
        HotbarMenu menu = new HotbarMenu();

        menu.setCloseHandler(closeContext -> {
            player.setGameMode(playerGameMode);
        });

        menu.setDefaultClickHandler(HotbarAction.DROP_ITEM, context -> {
            Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> roadMap.setEditMode(player.getUniqueId(), false), 1L);
        });

        menu.setItem(0, EditmodeUtils.NODE_TOOl);
        menu.setClickHandler(0, HotbarAction.LEFT_CLICK_ENTITY, context -> {
            Waypoint clickedFindable = getClickedFindable((Entity) context.getTarget());
            if (clickedFindable != null) {
                Player p = context.getPlayer();
                roadMap.deleteNode(clickedFindable);
                p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
            }
        });
        menu.setClickHandler(0, HotbarAction.RIGHT_CLICK_BLOCK, context -> {
            Waypoint last = pathPlayer.getLastSetFindable(roadMap);
            String name;
            if(last != null) {
                final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
                Matcher matcher = lastIntPattern.matcher(last.getNameFormat());
                if (matcher.find()) {
                    String first = matcher.group(0).replace(matcher.group(1), "");
                    name = first + (Integer.parseInt(matcher.group(1)) + 1);
                } else {
                    name = "node";
                }
            } else {
                name = "node";
            }
            openNodeNameMenu(context.getPlayer(), name, s -> {
                Waypoint f = roadMap.createNode(((Block) context.getTarget()).getLocation().toVector().add(new Vector(0.5, 1.5, 0.5)), s, null, null);
                f.setGroup(pathPlayer.getLastSetGroup(roadMap), true);
                pathPlayer.setLastSetFindable(f);
            });
        });
        menu.setClickHandler(0, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Integer npcId = CitizensHook.getInstance() == null ? null : CitizensHook.getInstance().getNpcID((Entity) context.getTarget());
            if(npcId != null) {
                openNodeTypeMenu(player, npcId);
            }
        });

        menu.setItem(1, EditmodeUtils.EDGE_TOOL);
        menu.setClickHandler(1, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Player p = context.getPlayer();
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked == null) {
                return;
            }
            if (this.firstFindableEdgeCreate == null) {
                firstFindableEdgeCreate = clicked;
                context.setItemStack(EditmodeUtils.EDGE_TOOL_GLOW);
            } else {
                if (firstFindableEdgeCreate.equals(clicked)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                if (firstFindableEdgeCreate.getEdges().contains(clicked.getNodeId())
                        || clicked.getEdges().contains(firstFindableEdgeCreate.getNodeId())) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                roadMap.connectNodes(firstFindableEdgeCreate, clicked);
                firstFindableEdgeCreate = null;
                context.setItemStack(EditmodeUtils.EDGE_TOOL);
            }
            p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1, 1);
        });
        menu.setClickHandler(1, HotbarAction.LEFT_CLICK_ENTITY, context -> {
            Waypoint f = getClickedFindable((Entity) context.getTarget());
            if(f != null) {
                roadMap.disconnectNode(f);
                return;
            }
            Player player = context.getPlayer();
            int clickedId = ((Entity) context.getTarget()).getEntityId();
            Pair<Waypoint, Waypoint> edge = roadMap.getEditModeEdgeArmorStands().keySet().stream()
                    .filter(key -> roadMap.getEditModeEdgeArmorStands().get(key).getEntityId() == clickedId)
                    .findAny().orElse(null);
            if (edge == null) {
                return;
            }
            roadMap.disconnectNodes(edge);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
        });
        menu.setClickHandler(1, HotbarAction.LEFT_CLICK_AIR, context -> {
            if (firstFindableEdgeCreate == null) {
                return;
            }
            firstFindableEdgeCreate = null;
            player.sendMessage(PathPlugin.PREFIX + "Verbinden abgebrochen");
            player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_BREAK, 1, 1);
            context.setItemStack(EditmodeUtils.EDGE_TOOL);
        });

        menu.setItem(7, EditmodeUtils.TP_TOOL);
        menu.setClickHandler(7, new HotbarAction[]{HotbarAction.RIGHT_CLICK_ENTITY, HotbarAction.RIGHT_CLICK_BLOCK, HotbarAction.RIGHT_CLICK_AIR}, context -> {
            double dist = -1;
            Waypoint nearest = null;
            for (Waypoint findable : roadMap.getNodes()) {
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

        menu.setItemAndClickHandler(5, EditmodeUtils.TANGENT_TOOL, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked != null) {
                openTangentStrengthMenu(context.getPlayer(), clicked);
            }
        });

        menu.setItemAndClickHandler(6, EditmodeUtils.PERMISSION_TOOL, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked != null) {
                openNodePermissionMenu(context.getPlayer(), clicked);
            }
        });

        menu.setItem(2, EditmodeUtils.GROUP_TOOL);
        menu.setClickHandler(2, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked == null) {
                return;
            }
            openGroupMenu(context.getPlayer(), clicked);
        });
        menu.setClickHandler(2, HotbarAction.LEFT_CLICK_ENTITY, context -> {
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked == null) {
                return;
            }
            clicked.setGroup(null, true, true);
            player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
        });

        menu.setItemAndClickHandler(3, EditmodeUtils.LAST_GROUP_TOOL, HotbarAction.RIGHT_CLICK_ENTITY, context -> {
            Waypoint clicked = getClickedFindable((Entity) context.getTarget());
            if (clicked == null) {
                return;
            }
            NodeGroup last = pathPlayer.getLastSetGroup(clicked.getRoadMap());
            if (last == null) {
                return;
            }
            clicked.setGroup(last.getGroupId(), true, true);
        });

        return menu;
    }

    private void openGroupMenu(Player player, Waypoint clicked) {

        PagedChestMenu groupMenu = new PagedChestMenu(Component.text("Node-Gruppen verwalten:"), 5);
        for (NodeGroup group : clicked.getRoadMap().getGroups().values()) {

            groupMenu.addMenuEntry(buildGroupItem(clicked, group), ClickType.LEFT, c -> {
                clicked.setGroup(group, true);
                pathPlayer.setLastSetGroup(group);
                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                c.getPlayer().closeInventory();
            });
        }
        groupMenu.setSortingComparator(Comparator.comparing(itemStack -> itemStack.getItemMeta().getDisplayName()));
        ItemStack create = ItemStackUtils.createItemStack(Material.EMERALD, ChatColor.GREEN + "Neue Gruppe", "");
        groupMenu.setNavigationEntry(8, create, c -> {
            openCreateGroupMenu(c.getPlayer(), clicked);
        });
        groupMenu.setNavigationEntry(7, ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.WHITE + "Gruppe zurücksetzen", ""), c -> {
            clicked.setGroup((NodeGroup) null, true);
            c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
            openGroupMenu(c.getPlayer(), clicked);
        });
        groupMenu.openInventory(player);
    }

    private void openCreateGroupMenu(Player player, Waypoint findable) {
        AnvilMenu menu = new AnvilMenu(Component.text("Nodegruppe erstellen:"));

        ItemStack result = new ItemStack(Material.CHEST);
        ItemStack info = result.clone();
        ItemStackUtils.setNameAndLore(info, ChatColor.WHITE + "Gruppe erstellen",
                CommandUtils.wordWrap(ChatColor.GRAY + "Gib einen eindeutigen Gruppennamen an. Gruppen dienen als findbare Orte auf der Karte. Zum Beispiel sollten alle Wegpunkte der Markthalle in einer Gruppe namens Markthalle sein.", "\n" + ChatColor.GRAY, 30));
        ItemStack error = ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.RED + "Ungültige Eingabe", ChatColor.GRAY + "Name vergeben.");

        menu.setItem(0, info);
        menu.setTextInputHandler((player1, s) -> {
            s = StringUtils.replaceSpaces(s);
            if (!roadMap.isGroupNameUnique(menu.getTextBoxText())) {
                menu.setItem(2, error);
            } else {
                ItemStackUtils.setNameAndLore(result, s, "");
                menu.setItem(2, result);
            }
        });
        menu.setItemAndClickHandler(2, result, context -> {
            if(menu.getTextBoxText() == null) {
                return;
            }
            String s = StringUtils.replaceSpaces(menu.getTextBoxText());
            Player p = context.getPlayer();
            if (!roadMap.isGroupNameUnique(s)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            NodeGroup group = roadMap.addFindableGroup(s, true);
            findable.setGroup(group, true);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            menu.closeInventory();
        });
        menu.open(player);
    }

    private ItemStack buildGroupItem(Waypoint clicked, NodeGroup group) {
        ItemStack stack = new ItemStack(group.isFindable() ? Material.CHEST : Material.ENDER_CHEST);
        StringBuilder lore = new StringBuilder(ChatColor.GRAY + "Findbar: " + PathPlugin.CHAT_COLOR_LIGHT + (group.isFindable() ? "An" : "Aus")
                + "\n" + ChatColor.GRAY + "Größe: " + PathPlugin.CHAT_COLOR_LIGHT + group.getFindables().size() + "\n" + ChatColor.GRAY + "Nodes in Gruppe:");

        int counter = 0;
        for (Waypoint f : group.getFindables()) {
            if (counter > 10) {
                lore.append("\n" + ChatColor.DARK_GRAY + "...");
                break;
            } else {
                lore.append("\n" + ChatColor.GRAY + "- ").append(PathPlugin.CHAT_COLOR_LIGHT).append(f.getNameFormat()).append(" (#").append(f.getNodeId()).append(")");
            }
            counter++;
        }
        ItemStackUtils.setNameAndLore(stack, PathPlugin.CHAT_COLOR_DARK + group.getNameFormat() + " (#" + group.getGroupId() + ")", lore.toString());
        if (clicked.getGroup() != null && clicked.getGroup().equals(group)) {
            ItemStackUtils.setGlowing(stack);
        }
        return stack;
    }

    private @Nullable
	Waypoint getClickedFindable(Entity entity) {
        Integer npcId = CitizensHook.getInstance() == null ? null : CitizensHook.getInstance().getNpcID(entity);
        if(npcId != null) {
            return roadMap.getNodes().stream()
                    .filter(f -> f instanceof NpcFindable)
                    .filter(f -> ((NpcFindable) f).getNpcId() == npcId)
                    .findAny().orElse(null);
        }
        Waypoint clickedFindable = null;
        for (Waypoint f : roadMap.getEditModeNodeArmorStands().keySet()) {
            Entity e = roadMap.getEditModeNodeArmorStands().get(f);
            if (e.equals(entity)) {
                clickedFindable = f;
                break;
            }
        }
        return clickedFindable;
    }

    private void openNodeTypeMenu(Player player, int npcId) {
        ChestMenu menu = new ChestMenu(Component.text("Wähle einen Wegpunkt-Typ:"), 1);
        menu.setItemAndClickHandler(0, ItemStackUtils.createItemStack(Material.GOLD_INGOT, "Händler", ""), context -> {
            openNodeNameMenu(context.getPlayer(), s -> {
                roadMap.createTraderFindable(npcId, s.equalsIgnoreCase("null") ? null : s, null, null);
            });
        });
        menu.setItemAndClickHandler(1, ItemStackUtils.createItemStack(Material.WRITABLE_BOOK, "Quest-NPC", ""), context -> {
            openNodeNameMenu(context.getPlayer(), s -> {
                roadMap.createQuestFindable(npcId, s.equalsIgnoreCase("null") ? null : s, null, null);
            });
        });
    }

    private void openNodeNameMenu(Player player, Consumer<String> nodeFactory) {
        openNodeNameMenu(player, "node", nodeFactory);
    }

    private void openNodeNameMenu(Player player, String nameInput, Consumer<String> nodeFactory) {
        AnvilMenu menu = new AnvilMenu(Component.text("Node erstellen:"));

        ItemStack result = HeadDBUtils.getHeadById(roadMap.getEditModeVisualizer().getNodeHeadId());
        ItemStack info = result.clone();
        ItemStackUtils.setNameAndLore(info, ChatColor.WHITE + nameInput,
                CommandUtils.wordWrap(ChatColor.GRAY + "Gib einen für diese Straßenkarte einzigartigen Namen oder 'null' an.", "\n" + ChatColor.GRAY, 30));
        ItemStack error = ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.RED + "Ungültige Eingabe", ChatColor.GRAY + "Name vergeben.");

        menu.setItem(0, info);
        menu.setTextInputHandler((player1, s) -> {
            s = StringUtils.replaceSpaces(s);
            if (!roadMap.isNodeNameUnique(menu.getTextBoxText())) {
                menu.setItem(2, error);
            } else {
                ItemStackUtils.setNameAndLore(result, s, "");
                menu.setItem(2, result);
            }
        });
        menu.setItemAndClickHandler(2, result, context -> {
            Player p = context.getPlayer();
            if(menu.getTextBoxText() == null) {
                return;
            }
            String s = StringUtils.replaceSpaces(menu.getTextBoxText());
            if (!roadMap.isNodeNameUnique(s)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            nodeFactory.accept(s);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            menu.closeInventory();
        });
        menu.open(player);
    }

    private void openTangentStrengthMenu(Player player, Waypoint findable) {
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

    private void openNodePermissionMenu(Player player, Waypoint findable) {
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
