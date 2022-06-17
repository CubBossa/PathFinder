package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapEditor;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.InventoryRow;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditModeMenu {

    private static final Pattern LAST_INT_PATTERN = Pattern.compile("[^0-9]+([0-9]+)$");

    public static BottomInventoryMenu createHotbarMenu(RoadMap roadMap, RoadMapEditor editor) {
        BottomInventoryMenu menu = new BottomInventoryMenu(InventoryRow.HOTBAR);
        AtomicReference<String> lastNamed = new AtomicReference<>("node_1");
        AtomicReference<Node> edgeStart = new AtomicReference<>();
        AtomicReference<Node> lastNode = new AtomicReference<>();
        AtomicReference<NamespacedKey> lastGroup = new AtomicReference<>();

        menu.setDefaultClickHandler(Action.HOTBAR_DROP, c -> {
            Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> editor.setEditMode(c.getPlayer().getUniqueId(), false), 1L);
        });

        menu.setButton(0, Button.builder()
                .withItemStack(EditmodeUtils.NODE_TOOl)
                .withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
                    Player p = context.getPlayer();
                    roadMap.removeNode(context.getTarget());
                    p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
                }).withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> {
                    String name = lastNamed.get();
                    Matcher matcher = LAST_INT_PATTERN.matcher(name);
                    if (matcher.find()) {
                        String first = matcher.group(0).replace(matcher.group(1), "");
                        name = first + (Integer.parseInt(matcher.group(1)) + 1);
                    }

                    openNodeNameMenu(context.getPlayer(), name, s -> {
                        Node node = roadMap.createNode((context.getTarget()).getLocation().toVector().add(new Vector(0.5, 1.5, 0.5)), s, null, null);
                        node.setGroupKey(lastGroup.get());
                        lastNode.set(node);
                    });
                }));


        menu.setButton(1, Button.builder()
                .withItemStack(EditmodeUtils.EDGE_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, c -> {
                    Player p = c.getPlayer();

                    if (edgeStart.get() == null) {
                        edgeStart.set(c.getTarget());
                        c.setItemStack(EditmodeUtils.EDGE_TOOL_GLOW);
                    } else {
                        if (edgeStart.get().equals(c.getTarget())) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        if (edgeStart.get().getEdges().stream().anyMatch(e -> e.getEnd().equals(c.getTarget()))) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        roadMap.connectNodes(edgeStart.get(), c.getTarget());
                        edgeStart.set(null);
                        c.setItemStack(EditmodeUtils.EDGE_TOOL);
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1, 1);
                }).withClickHandler(Action.RIGHT_CLICK_AIR, context -> {
                    if (edgeStart.get() == null) {
                        return;
                    }
                    Player player = context.getPlayer();
                    edgeStart.set(null);
                    player.sendMessage(PathPlugin.PREFIX + "Verbinden abgebrochen");
                    player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_BREAK, 1, 1);
                    context.setItemStack(EditmodeUtils.EDGE_TOOL);

                }).withClickHandler(ClientNodeHandler.LEFT_CLICK_EDGE, context -> {
                    Player player = context.getPlayer();

                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
                }).withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
                    Player player = context.getPlayer();
                    roadMap.disconnectNode(context.getTarget());
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
                }));


        menu.setButton(7, Button.builder()
                .withItemStack(EditmodeUtils.TP_TOOL)
                .withClickHandler(context -> {
                    double dist = -1;
                    Node nearest = null;
                    Vector vecP = context.getPlayer().getLocation().toVector();
                    for (Node node : roadMap.getNodes()) {
                        double d = node.getPosition().distance(vecP);
                        if (dist == -1 || d < dist) {
                            nearest = node;
                            dist = d;
                        }
                    }
                    if (nearest == null) {
                        return;
                    }
                    Player p = context.getPlayer();
                    Location newLoc = nearest.getLocation().setDirection(p.getLocation().getDirection());
                    p.teleport(newLoc);
                    p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1, 1);
                }, Action.RIGHT_CLICK_ENTITY, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR));

        menu.setButton(5, Button.builder()
                .withItemStack(EditmodeUtils.TANGENT_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context ->
                        openTangentStrengthMenu(context.getPlayer(), context.getTarget())));

        menu.setButton(6, Button.builder()
                .withItemStack(EditmodeUtils.PERMISSION_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context ->
                        openNodePermissionMenu(context.getPlayer(), context.getTarget())));

        menu.setButton(2, Button.builder()
                .withItemStack(EditmodeUtils.GROUP_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context ->
                        openGroupMenu(context.getPlayer(), context.getTarget()))
                .withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
                    context.getTarget().setGroupKey(null);
                    context.getPlayer().playSound(context.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1, 1);
                }));

        menu.setButton(3, Button.builder()
                .withItemStack(EditmodeUtils.LAST_GROUP_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context -> context.getTarget().setGroupKey(lastGroup.get())));

        return menu;
    }

    private static void openGroupMenu(Player player, RoadMap roadMap, Node clicked) {

        Menu menu = new ListMenu(Component.text("Node-Gruppen verwalten:"), 5);
        for (NodeGroup group : roadMap.getGroups().values()) {

            menu.addMenuEntry(buildGroupItem(clicked, group), ClickType.LEFT, c -> {
                clicked.setGroup(group, true);
                pathPlayer.setLastSetGroup(group);
                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                c.getPlayer().closeInventory();
            });
        }
        ItemStack create = ItemStackUtils.createItemStack(Material.EMERALD, ChatColor.GREEN + "Neue Gruppe", "");
        menu.setNavigationEntry(8, create, c -> {
            openCreateGroupMenu(c.getPlayer(), clicked);
        });
        menu.setNavigationEntry(7, ItemStackUtils.createItemStack(Material.BARRIER, ChatColor.WHITE + "Gruppe zurücksetzen", ""), c -> {
            clicked.setGroup((NodeGroup) null, true);
            c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
            openGroupMenu(c.getPlayer(), clicked);
        });
        menu.open(player);
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

    private static void openNodeNameMenu(Player player, Consumer<String> nodeFactory) {
        openNodeNameMenu(player, "node", nodeFactory);
    }

    private static void openNodeNameMenu(Player player, String nameInput, Consumer<String> nodeFactory) {
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

    private static void openTangentStrengthMenu(Player player, Node findable) {
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

    private static void openNodePermissionMenu(Player player, Node findable) {
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
