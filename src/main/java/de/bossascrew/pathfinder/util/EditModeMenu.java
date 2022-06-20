package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.events.node.NodeRenameEvent;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapEditor;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.InventoryRow;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.menuframework.inventory.implementations.AnvilMenu;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.translations.TranslatedItem;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditModeMenu {

    private static final Pattern LAST_INT_PATTERN = Pattern.compile("[^0-9]+([0-9]+)$");

    RoadMap roadMap;
    String lastNamed = "node_1";
    Node edgeStart = null;
    Node lastNode = null;
    NamespacedKey lastGroup = null;

    public EditModeMenu(RoadMap roadMap) {
        this.roadMap = roadMap;
    }

    public BottomInventoryMenu createHotbarMenu(RoadMapEditor editor) {
        BottomInventoryMenu menu = new BottomInventoryMenu(InventoryRow.HOTBAR);

        menu.setDefaultClickHandler(Action.HOTBAR_DROP, c -> {
            Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> editor.setEditMode(c.getPlayer().getUniqueId(), false), 1L);
        });

        menu.setButton(0, Button.builder()
                .withItemStack(EditmodeUtils.NODE_TOOL)
                .withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
                    Player p = context.getPlayer();
                    roadMap.removeNode(context.getTarget());
                    p.playSound(p.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
                })
                .withClickHandler(Action.RIGHT_CLICK_BLOCK, context -> {
                    String name = lastNamed;
                    Matcher matcher = LAST_INT_PATTERN.matcher(name);
                    if (matcher.find()) {
                        String first = matcher.group(0).replace(matcher.group(1), "");
                        name = first + (Integer.parseInt(matcher.group(1)) + 1);
                    }

                    openNodeNameMenu(context.getPlayer(), name, s -> {
                        Node node = roadMap.createNode((context.getTarget()).getLocation().toVector().add(new Vector(0.5, 1.5, 0.5)), s, null, null);
                        if (node != null) {
                            node.setGroupKey(lastGroup);
                            lastNode = node;
                        }
                    });
                }));


        menu.setButton(1, Button.builder()
                .withItemStack(() -> edgeStart == null ? EditmodeUtils.EDGE_TOOL : EditmodeUtils.EDGE_TOOL_GLOW)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, c -> {
                    Player p = c.getPlayer();

                    if (edgeStart == null) {
                        edgeStart = c.getTarget();
                    } else {
                        if (edgeStart.equals(c.getTarget())) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        if (edgeStart.getEdges().stream().anyMatch(e -> e.getEnd().equals(c.getTarget()))) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            return;
                        }
                        roadMap.connectNodes(edgeStart, c.getTarget());
                        edgeStart = null;
                    }
                    c.getMenu().refresh(c.getSlot());
                    p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 1, 1);
                })
                .withClickHandler(Action.LEFT_CLICK_AIR, context -> {
                    if (edgeStart == null) {
                        return;
                    }
                    Player player = context.getPlayer();
                    edgeStart = null;
                    TranslationHandler.getInstance().sendMessage(Messages.E_EDGE_TOOL_CANCELLED, player);
                    player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_BREAK, 1, 1);
                    context.getMenu().refresh(context.getSlot());

                })
                .withClickHandler(ClientNodeHandler.LEFT_CLICK_EDGE, context -> {
                    Player player = context.getPlayer();
                    roadMap.disconnectNodes(context.getTarget());
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
                })
                .withClickHandler(ClientNodeHandler.LEFT_CLICK_NODE, context -> {
                    Player player = context.getPlayer();
                    roadMap.disconnectNode(context.getTarget());
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 1, 1);
                }));


        menu.setButton(8, Button.builder()
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

        menu.setButton(7, Button.builder()
                .withItemStack(EditmodeUtils.RENAME_TOOL)
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context -> {
                    openNodeNameMenu(context.getPlayer(), context.getTarget().getNameFormat(), string -> {

                        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {

                            NodeRenameEvent event = new NodeRenameEvent(context.getTarget(), string);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }
                            context.getTarget().setNameFormat(string);
                        });
                    });
                }));

        menu.setButton(5, Button.builder()
                .withItemStack(EditmodeUtils.CURVE_TOOL)
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
                .withClickHandler(ClientNodeHandler.RIGHT_CLICK_NODE, context -> context.getTarget().setGroupKey(lastGroup)));

        return menu;
    }

    private void openGroupMenu(Player player, Node node) {

        ListMenu menu = new ListMenu(Component.text("Node-Gruppen verwalten:"), 5);
        for (NodeGroup group : roadMap.getGroups().values()) {

            menu.addListEntry(Button.builder()
                    .withItemStack(() -> {
                        ItemStack stack = new TranslatedItem(group.isFindable() ? Material.CHEST : Material.ENDER_CHEST, Messages.E_SUB_GROUP_RESET_N, Messages.E_SUB_GROUP_RESET_L).createItem();
                        if (node.getGroupKey() != null && node.getGroupKey().equals(group.getKey())) {
                            stack = de.bossascrew.pathfinder.util.ItemStackUtils.setGlow(stack);
                        }
                        return stack;
                    })
                    .withClickHandler(Action.LEFT, c -> {
                        node.setGroupKey(group.getKey());
                        lastGroup = group.getKey();
                        c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                        menu.close(player);
                    }));
        }
        menu.addPreset(presetApplier -> {
            presetApplier.addItemOnTop(4 * 9 + 7, new TranslatedItem(Material.BARRIER, Messages.E_SUB_GROUP_RESET_N, Messages.E_SUB_GROUP_RESET_L).createItem());
            presetApplier.addClickHandlerOnTop(4 * 9 + 7, Action.LEFT, c -> {
                node.setGroupKey(null);
                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
            });

            presetApplier.addItemOnTop(4 * 9 + 8, new TranslatedItem(Material.EMERALD, Messages.E_SUB_GROUP_NEW_N, Messages.E_SUB_GROUP_NEW_L).createItem());
            presetApplier.addClickHandlerOnTop(4 * 9 + 8, Action.LEFT, c -> openCreateGroupMenu(c.getPlayer(), node));
        });
        menu.open(player);
    }

    private void openCreateGroupMenu(Player player, Node node) {
        AnvilMenu menu = newAnvilMenu(Component.text("Nodegruppe erstellen:"), "group_x", AnvilInputValidator.VALIDATE_KEY);

        menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
            NamespacedKey key = AnvilInputValidator.VALIDATE_KEY.getInputParser().apply(s.getTarget());
            if (key == null || roadMap.getNodeGroup(key) != null) {
                s.getPlayer().playSound(s.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
            NodeGroup group = roadMap.createNodeGroup(key, true);
            node.setGroupKey(group.getKey());
        });
        menu.open(player);
    }

    private void openNodeNameMenu(Player player, Consumer<String> onSuccess) {
        openNodeNameMenu(player, "Waypoint X", onSuccess);
    }

    private void openNodeNameMenu(Player player, String nameInput, Consumer<String> onSuccess) {
        AnvilMenu menu = newAnvilMenu(Component.text("Node erstellen:"), nameInput);

        menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
            onSuccess.accept(s.getTarget());
            s.getMenu().close(s.getPlayer());
        });
        menu.open(player);
    }

    private void openTangentStrengthMenu(Player player, Node findable) {
        AnvilMenu menu = newAnvilMenu(Component.text("Rundung einstellen:"), "3.0", AnvilInputValidator.VALIDATE_FLOAT);

        menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
            if (!AnvilInputValidator.VALIDATE_FLOAT.getInputValidator().test(s.getTarget())) {
                s.getPlayer().playSound(s.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            Double strength = s.getTarget().equalsIgnoreCase("null") ? null : Double.parseDouble(s.getTarget());
            findable.setBezierTangentLength(strength);
            menu.close(s.getPlayer());
        });
        menu.open(player);
    }

    private void openNodePermissionMenu(Player player, Node node) {
        AnvilMenu menu = newAnvilMenu(Component.text("Permission setzen:"), "null", AnvilInputValidator.VALIDATE_PERMISSION);
        menu.setItem(0, new ItemStack(Material.PAPER));
        menu.setOutputClickHandler(AnvilMenu.CONFIRM, s -> {
            Player p = s.getPlayer();
            node.setPermission(s.getTarget().equalsIgnoreCase("null") ? null : s.getTarget());
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            menu.close(p);
        });
        menu.open(player);
    }

    public static AnvilMenu newAnvilMenu(ComponentLike title, String suggestion) {
        return newAnvilMenu(title, suggestion, null);
    }

    public static <T> AnvilMenu newAnvilMenu(ComponentLike title, String suggestion, AnvilInputValidator<T> validator) {
        AnvilMenu menu = new AnvilMenu(title, suggestion);
        menu.addPreset(MenuPresets.back(1, Action.LEFT));
        menu.setClickHandler(0, AnvilMenu.WRITE, s -> {
            if (validator != null && !validator.getInputValidator().test(s.getTarget())) {
                menu.setItem(2, ItemStackUtils.createErrorItem(Messages.GEN_GUI_WARNING_N, Messages.GEN_GUI_WARNING_L
                        .format(TagResolver.resolver("format", Tag.inserting(validator.getRequiredFormat())))));
            } else {
                menu.setItem(2, ItemStackUtils.createCustomHead(ItemStackUtils.HEAD_URL_LETTER_CHECK_MARK, Messages.GEN_GUI_ACCEPT_N, Messages.GEN_GUI_ACCEPT_L));
            }
            menu.refresh(2);
        });
        return menu;
    }
}
