package de.bossascrew.pathfinder.commands;

import com.google.common.collect.Lists;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.inventory.menu.PagedChestMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.bukkit.util.ItemStackUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.AStarUtils;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.cubbossa.menuframework.chat.ComponentMenu;
import de.cubbossa.menuframework.chat.TextMenu;
import de.cubbossa.menuframework.inventory.implementations.RectInventoryMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindCommand extends BaseCommand {

    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        TextMenu menu = new TextMenu("Finde Orte einer Stadtkarte mit folgenden Befehlen:");
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_LOCATIONS)) {
            menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find ort <Ort>")));
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_ITEMS)) {
            if (PathPlugin.getInstance().isTraders() || PathPlugin.getInstance().isQuests() || (PathPlugin.getInstance().isBentobox()) && PathPlugin.getInstance().isChestShop()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find item <Item>")));
            }
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_QUESTS)) {
            if (PathPlugin.getInstance().isQuests()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find quest <Quest>")));
            }
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_TRADERS)) {
            if (PathPlugin.getInstance().isTraders()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find shop <Shop>")));
            }
        }
        TextMenu menu1 = new TextMenu("Info über gefundene Orte mit: ");
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_INFO) &&
                RoadMapHandler.getInstance().getRoadMaps().stream().anyMatch(RoadMap::isFindableNodes)) {
            menu1.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find info", ClickEvent.Action.RUN_COMMAND)));
        }
        if (menu.hasSubs()) {
            PlayerUtils.sendComponents(player, menu.toComponents());
        }
        if (menu1.hasSubs()) {
            PlayerUtils.sendComponents(player, menu1.toComponents());
        }
        if (!menu.hasSubs() && !menu1.hasSubs()) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Es wurden keine Befehle gefunden.");
        }
    }

    @Subcommand("info")
    @Syntax("[<Straßenkarte>]")
    @CommandCompletion(PathPlugin.COMPLETE_ACTIVE_ROADMAPS)
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_INFO)
    public void onInfo(Player player, @Optional RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        TextMenu menu = new TextMenu("Straßenkarten erkundet:");
        for (RoadMap rm : roadMap == null ? RoadMapHandler.getInstance().getRoadMaps()
                .stream().filter(RoadMap::isFindableNodes).collect(Collectors.toList()) : Lists.newArrayList(roadMap)) {
            double percent = 100 * ((double) pathPlayer.getFoundAmount(rm)) / rm.getMaxFoundSize();
            menu.addSub(new TextMenu(ChatColor.GRAY + rm.getNameFormat() + ": " + ChatColor.WHITE + String.format("%,.2f", percent) + "%"));
        }
        if (menu.hasSubs()) {
            PlayerUtils.sendComponents(player, menu.toComponents());
        } else {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Keine Straßenkarten gefunden.");
        }
    }

    @Subcommand("style")
    @Syntax("<Straßenkarte>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_STYLE)
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onStyle(Player player, RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        openStyleMenu(player, pathPlayer, roadMap);
    }

    Map<Player, Long> styleMenuCooldown = new ConcurrentHashMap<>();

    private void openStyleMenu(Player player, PathPlayer pathPlayer, RoadMap roadMap) {
        RectInventoryMenu menu = new RectInventoryMenu(Component.text("Wähle deinen Partikelstyle"), 3);
        PathVisualizer actual = pathPlayer.getVisualizer(roadMap);

        Collection<PathVisualizer> visualizers = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getRoadmapId(), new ArrayList<>());
        for (PathVisualizer visualizer : visualizers) {
            String perm = visualizer.getPickPermission();

            boolean hasPerm = perm == null || player.hasPermission(perm);
            boolean def = false, spender = false, spender2 = false;
            if (!hasPerm) {
                GroupManager groupManager = BukkitMain.getInstance().getLuckPerms().getGroupManager();
                Group defaultGroup = groupManager.getGroup("default");
                def = defaultGroup.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                if (!def) {
                    Group spenderGroup = groupManager.getGroup("spender");
                    spender = spenderGroup.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                    if (!spender) {
                        Group spender2Group = groupManager.getGroup("spender2");
                        spender2 = spender2Group.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                    }
                }
            }

            ItemStack stack = new ItemStack(visualizer.getIconType());
            ItemMeta m = stack.getItemMeta();
            m.displayName(visualizer.getDisplayName());
            if (spender) {
                m.lore(Lists.newArrayList(Component.text("Ab Matrose erhältlich.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));
            } else if (spender2) {
                m.lore(Lists.newArrayList(Component.text("Ab Maat erhältlich.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));
            }
            stack.setItemMeta(m);

            if (actual.equals(visualizer)) {
                ItemStackUtils.setGlowing(stack);
            }
            menu.addMenuEntry(stack, /*hasPerm ? 0 : spender ? 1 : spender2 ? 2 : 3, */context -> {
                if (!hasPerm) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                if(styleMenuCooldown.containsKey(player)) {
                    if(System.currentTimeMillis() - styleMenuCooldown.get(player) < 1000) {
                        PlayerUtils.sendMessage(player, ChatColor.RED + "Bitte warte eine Sekunde, bevor du deinen Pfad neu setzt.");
                        return;
                    }
                }
                styleMenuCooldown.put(player, System.currentTimeMillis());
                pathPlayer.setVisualizer(roadMap, visualizer);
                player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1, 1);
                PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikelstyle ausgewählt: " + visualizer.getName());
                openStyleMenu(player, pathPlayer, roadMap);
            });
        }
        menu.open(player);
    }

    @Subcommand("ort")
    @Syntax("<Ort>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_LOCATIONS)
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_LOCATIONS)
    public void onFindeOrt(Player player, String searched) {
        RoadMap roadMap = CommandUtils.getAnyRoadMap(player.getWorld());
        if (roadMap == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Keine Straßenkarte gefunden.");
            return;
        }
        if (!roadMap.getWorld().equals(player.getWorld())) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Diese Straßenkarte liegt nicht in deiner aktuellen Welt.");
            return;
        }
        PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
        if(pp == null) {
            return;
        }

        Node f = roadMap.getFindables().stream()
                .filter(findable -> findable.getGroup() == null)
                .filter(findable -> findable.getNameFormat().equalsIgnoreCase(searched))
                .findFirst().orElse(null);
        if(f == null) {
            FindableGroup group = roadMap.getGroups().values().stream()
                    .filter(FindableGroup::isFindable)
                    .filter(g -> g.getName().equalsIgnoreCase(searched))
                    .filter(g -> pp.hasFound(g.getDatabaseId(), true))
                    .findAny().orElse(null);
            if(group == null) {
                PlayerUtils.sendMessage(player, ChatColor.RED + "Es gibt kein Ziel mit diesem Namen.");
                return;
            }
            f = group.getFindables().stream().findAny().orElse(null);
        }
        if (f == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Ein Fehler ist aufgetreten.");
            return;
        }
        AStarUtils.startPath(player, f, true);
    }
}
