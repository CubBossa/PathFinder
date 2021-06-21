package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.util.PagedChatMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@CommandAlias("waypoint|wp|node")
public class WaypointCommand extends BaseCommand {

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.waypoint.create")
    public void onCreate(Player player, @Single String name) {
        RoadMap roadMap = getRoadMap(player);
        if (roadMap == null) {
            return;
        }
        if (!roadMap.isNodeNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben.");
            return;
        }
        roadMap.createNode(player.getLocation().toVector(), name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.delete")
    @CommandCompletion(PathPlugin.COMPLETE_NODES)
    public void onDelete(Player player, Node node) {
        RoadMap roadMap = getRoadMap(player);
        if (roadMap == null) {
            return;
        }

        roadMap.deleteFindable(node);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich gelöscht: " + ChatColor.GREEN + node.getName());
    }

    @Subcommand("tphere")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.tphere")
    @CommandCompletion(PathPlugin.COMPLETE_NODES)
    public void onTphere(Player player, Node node) {
        node.setVector(player.getLocation().toVector());
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich zu deiner Position verschoben");
    }

    @Subcommand("rename")
    @Syntax("<Node> <neuer Name>")
    @CommandPermission("bcrew.command.waypoint.rename")
    @CommandCompletion(PathPlugin.COMPLETE_NODES)
    public void onRename(Player player, Node node, @Single String newName) {

        RoadMap roadMap = getRoadMap(player);
        if (roadMap == null) {
            return;
        }

        if (!roadMap.isNodeNameUnique(newName)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben.");
            return;
        }
        node.setName(newName);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Name erfolgreich geändert zu: " +
                ChatColor.GREEN + newName);
    }

    @Subcommand("connect")
    @Syntax("<Node> <Node>")
    @CommandPermission("bcrew.command.waypoint.connect")
    @CommandCompletion(PathPlugin.COMPLETE_NODES + " " + PathPlugin.COMPLETE_NODES)
    public void onConnect(Player player, Node a, Node b) {
        if (a.getDatabaseId() == b.getDatabaseId()) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Die Wegpunkte sind identisch.");
            return;
        }
        if (a.getEdges().contains(b.getDatabaseId())
                || b.getEdges().contains(a.getDatabaseId())) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Die Wegpunkte sind bereits verunden.");
            return;
        }
        RoadMap roadMap = getRoadMap(player);
        if (roadMap == null) {
            return;
        }
        roadMap.connectNodes(a, b);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Wegpunke erfolgreich verbunden.");
    }

    @Subcommand("setpermission")
    @Syntax("<Node> <Permission>")
    @CommandPermission("bcrew.command.waypoint.setpermission")
    @CommandCompletion(PathPlugin.COMPLETE_NODES + " some.custom.permission")
    public void onSetPermission(Player player, Node node, @Single String perm) {
        if (node == null) {
            return;
        }
        node.setPermission(perm);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Permission erfolgreich gesetzt: " + ChatColor.GREEN + perm);
    }

    @Subcommand("settangent")
    @Syntax("<Node> <Rundungsstärke>")
    @CommandPermission("bcrew.command.waypoint.settangent")
    @CommandCompletion(PathPlugin.COMPLETE_NODES)
    public void onSetTangent(Player player, Node node, double strength) {
        if (node == null) {
            return;
        }
        node.setBezierTangentLength(strength);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Rundungsstärke gesetzt: " + ChatColor.GREEN + strength);
    }

    @Subcommand("resettangent")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.resettangent")
    @CommandCompletion(PathPlugin.COMPLETE_NODES)
    public void onSetTangent(Player player, Node node) {
        if (node == null) {
            return;
        }
        node.setBezierTangentLength(null);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Rundungsstärke zurückgesetzt");
    }

    @Subcommand("list")
    @Syntax("[<Seite>]")
    @CommandPermission("bcrew.command.waypoint.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = getRoadMap(player);
        if (roadMap == null) {
            return;
        }

        int page = 0;
        if (pageInput != null) {
            page = pageInput - 1;
        }

        Component title = Component.text("===] ", NamedTextColor.DARK_GRAY)
                .append(Component.text(roadMap.getName() + " Wegpunkte", NamedTextColor.WHITE)
                        .decoration(TextDecoration.UNDERLINED, true))
                .append(Component.text(" [==", NamedTextColor.DARK_GRAY));

        PagedChatMenu menu = new PagedChatMenu(title, 20, "waypoint list %page%");

        //outofbounds verhindern
        if (page >= menu.getPageCount()) {
            page = menu.getPageCount() - 1;
        }
        if (page < 1) {
            page = 1;
        }

        for (Findable findable : roadMap.getFindables()) {

            FindableGroup group = roadMap.getFindableGroup(findable);
            String groupName = "-";
            if (group != null) {
                groupName = group.getName();
            }

            Component entry = Component.text(findable.getName(), NamedTextColor.GREEN)
                    .append(getSeperator()).append(Component.text("ID: ", NamedTextColor.GRAY))
                    .append(Component.text(findable.getDatabaseId(), NamedTextColor.DARK_GREEN))
                    .append(getSeperator().append(Component.text("Gruppe: ", NamedTextColor.GRAY)))
                    .append(Component.text(groupName, NamedTextColor.DARK_GREEN));
            menu.addEntry(entry);
        }
        player.sendMessage(menu.getPage(page));
    }

    private Component getSeperator() {
        return Component.text(" | ", NamedTextColor.DARK_GRAY);
    }

    private @Nullable
    RoadMap getRoadMap(Player player) {
        PathPlayer pplayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (pplayer == null) {
            return null;
        }
        if (pplayer.getSelectedRoadMapId() == null) {
            return null;
        }
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMapId());
        if (roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Du musst eine RoadMap auswählen. (/roadmap select)");
            return null;
        }
        return roadMap;
    }

    //TODO
    //info
    //list
    //group clear
    //group set <group>
    //disconnect <node>|*
}
