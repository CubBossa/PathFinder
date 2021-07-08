package de.bossascrew.pathfinder.commands;

import com.google.common.collect.Lists;
import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CommandAlias("waypoint|wp|node|findable")
public class WaypointCommand extends BaseCommand {

    @Subcommand("info")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.info")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
    public void onInfo(Player player, Node node) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        ComponentMenu menu = new ComponentMenu(Component.text("Wegpunkt ")
                .append(Component.text(node.getName() + "(#" + node.getDatabaseId() + ")", NamedTextColor.DARK_GREEN))
                .hoverEvent(HoverEvent.showText(Component.text("Name ändern")))
                .clickEvent(ClickEvent.suggestCommand("/waypoint set name " + node.getName() + " <Neuer Name>")));

        FindableGroup group = roadMap.getFindableGroup(node.getNodeGroupId());
        menu.addSub(getSub("Gruppe: ", group == null ? Component.text("-", NamedTextColor.GRAY) : Component.text(group.getName(), NamedTextColor.GREEN), "Gruppe ändern",
                "/waypoint set nodegroup " + node.getName() + " <NodeGroup>"));

        menu.addSub(getSub("Permission: ", node.getPermission(), "Perission ändern",
                "/waypoint set permission " + node.getName() + " <Permission>"));

        menu.addSub(getSub("Rundung: ", node.getBezierTangentLength() == null ? "null«" + node.getBezierTangentLengthOrDefault() : node.getBezierTangentLength() + "",
                "Tangentenwichtung ändern",
                "/waypoint set tangent " + node.getName() + " <Stärke>"));

        menu.addSub(new ComponentMenu(Component.text("Position: ")
                .append(Component.text(node.getRoadMap().getWorld().getName())
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%,.1f", node.getVector().getX()), NamedTextColor.GREEN))
                        .append(Component.text(", ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%,.1f", node.getVector().getY()), NamedTextColor.GREEN))
                        .append(Component.text(", ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%,.1f", node.getVector().getZ()), NamedTextColor.GREEN)))
                .hoverEvent(Component.text("Klicke zum Teleportieren"))
                .clickEvent(ClickEvent.runCommand("/teleport " + node.getRoadMap().getWorld().getName() + " " + node.getVector().getX() + " "
                        + node.getVector().getY() + " " + node.getVector().getZ()))));

        Menu edges = new Menu("Verbindungen: " + (node.getEdges().isEmpty() ? ChatColor.GRAY + "-" : ""));
        for (int edge : node.getEdges()) {
            Findable target = roadMap.getFindable(edge);
            if (target == null) {
                continue;
            }
            Component targetComp = Component.text(target.getName() + "(#" + target.getDatabaseId() + ")", CommandUtils.NULL_COLOR)
                    .append(Component.text(", Entfernung: " + String.format("%,.2f", node.getVector().distance(target.getVector())) + "m ", NamedTextColor.GRAY))
                    .append(Component.text("[X]", NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(Component.text("Verbindung löschen")))
                            .clickEvent(ClickEvent.runCommand("/waypoint disconnect " + node.getName() + " " + target.getName())));
            edges.addSub(new ComponentMenu(targetComp));
        }
        menu.addSub(edges);
        PlayerUtils.sendComponents(player, menu.toComponents());
    }

    private ComponentMenu getSub(String attributeName, String value, String hover, String command) {
        return getSub(attributeName, Component.text(value, NamedTextColor.GREEN), hover, command);
    }

    private ComponentMenu getSub(String attributeName, Component value, String hover, String command) {
        return new ComponentMenu(Component.text(attributeName)
                .append(value)
                .hoverEvent(HoverEvent.showText(Component.text(hover)))
                .clickEvent(ClickEvent.suggestCommand(command)));
    }

    @Subcommand("create default")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.waypoint.create")
    public void onCreate(Player player, @Single String name) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
        if (!roadMap.isNodeNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben.");
            return;
        }
        roadMap.createNode(player.getLocation().toVector().add(new Vector(0, 1, 0)), name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.delete")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
    public void onDelete(Player player, Node node) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        roadMap.deleteFindable(node);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich gelöscht: " + ChatColor.GREEN + node.getName());
    }

    @Subcommand("tphere")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.tphere")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
    public void onTphere(Player player, Node node) {
        node.setVector(player.getLocation().toVector());
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich zu deiner Position verschoben");
    }

    @Subcommand("list")
    @Syntax("[<Seite>]")
    @CommandPermission("bcrew.command.waypoint.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        Component nodes = Component.text("Wegpunkte: " + ChatColor.GREEN + roadMap.getName()).append(Component.newline());
        int count = 0;
        for (Findable findable : roadMap.getFindables()) {
            nodes = nodes.append(Component.text(findable.getName(), NamedTextColor.WHITE))
                    .hoverEvent(HoverEvent.showText(Component.text("Informationen anzeigen")))
                    .clickEvent(ClickEvent.runCommand("/waypoint info " + findable.getName()));
            if (count < roadMap.getFindables().size() - 1) {
                nodes = nodes.append(Component.text(", ", NamedTextColor.GRAY));
            }
            count++;
        }
        PlayerUtils.sendComponents(player, Lists.newArrayList(nodes));
    }

    @Subcommand("connect")
    @Syntax("<Node> <Node>")
    @CommandPermission("bcrew.command.waypoint.connect")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLES + " " + PathPlugin.COMPLETE_FINDABLES)
    public void onConnect(Player player, Findable a, Findable b) {
        if (a.getDatabaseId() == b.getDatabaseId()) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Die Wegpunkte sind identisch.");
            return;
        }
        if (a.getEdges().contains(b.getDatabaseId())
                || b.getEdges().contains(a.getDatabaseId())) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Die Wegpunkte sind bereits verunden.");
            return;
        }
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
        roadMap.connectNodes(a, b);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Wegpunke erfolgreich verbunden.");
    }

    @Subcommand("disconnect")
    @Syntax("<Node> <Node>")
    @CommandPermission("bcrew.command.waypoint.disconnect")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLES + " " + PathPlugin.COMPLETE_FINDABLES_CONNECTED)
    public void onDisconnect(Player player, Findable a, Findable b) {
        if (!a.getEdges().contains(b.getDatabaseId())) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Der Wegpunkt " + b.getName() + " ist nicht mit " + a.getName() + " verbunden.");
            return;
        }
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
        roadMap.disconnectNodes(a, b);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Verbindung aufgelöst.");
    }

    @Subcommand("set")
    public class WaypointSetCommand extends BaseCommand {

        @Subcommand("name")
        @Syntax("<Node> <neuer Name>")
        @CommandPermission("bcrew.command.waypoint.rename")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
        public void onRename(Player player, Node node, @Single String newName) {
            RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

            if (!roadMap.isNodeNameUnique(newName)) {
                PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben.");
                return;
            }
            node.setName(newName);
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Name erfolgreich geändert zu: " +
                    ChatColor.GREEN + newName);
        }

        @Subcommand("permission")
        @Syntax("<Node> <Permission>")
        @CommandPermission("bcrew.command.waypoint.setpermission")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLES + " some.custom.permission")
        public void onSetPermission(Player player, Node node, @Single String perm) {
            if (perm.equalsIgnoreCase("null")) {
                perm = null;
            }
            node.setPermission(perm);
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Permission erfolgreich gesetzt: " + ChatColor.GREEN + perm);
        }

        @Subcommand("tangent")
        @Syntax("<Node> <Rundungsstärke>")
        @CommandPermission("bcrew.command.waypoint.settangent")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
        public void onSetTangent(Player player, Findable findable, Double strength) {
            findable.setBezierTangentLength(strength);
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Rundungsstärke gesetzt: " + ChatColor.GREEN + strength);
        }

        @Subcommand("group")
        @Syntax("<Node> <Gruppe>")
        @CommandCompletion(PathPlugin.COMPLETE_FINDABLES + " null|" + PathPlugin.COMPLETE_FINDABLE_GROUPS)
        public void onSetGroup(CommandSender sender, Findable findable, @Single String groupName) {

            FindableGroup group = null;
            if(groupName != null) {
                group = CommandUtils.getSelectedRoadMap(sender).getFindableGroup(groupName);
            }
            if(group == null) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Es existiert keine Node-Gruppe mit diesem Namen.");
                return;
            }
            findable.setGroup(group);
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Gruppe gesetzt: " + groupName);
        }
    }
}
