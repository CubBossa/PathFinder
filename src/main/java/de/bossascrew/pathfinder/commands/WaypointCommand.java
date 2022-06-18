package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

@CommandAlias("waypoint|node|findable")
public class WaypointCommand extends BaseCommand {

    @Subcommand("info")
    @Syntax("<Node>")
    @CommandPermission("pathfinder.command.waypoint.info")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
    public void onInfo(Player player, NodeSelection selection) {

        if (selection.size() > 1) {
            //TODO selection choice
            return;
        }
        Node node = selection.get(0);
        FormattedMessage message = Messages.CMD_N_INFO.format(TagResolver.builder()
                .tag("id", Tag.preProcessParsed(node.getNodeId() + ""))
                .tag("name", Tag.inserting(node.getDisplayName()))
                .tag("roadmap", Tag.inserting(Messages.formatKey(node.getRoadMapKey())))
                .tag("permission", Tag.inserting(Messages.formatPermission(node.getPermission())))
                .tag("group", Tag.inserting(Messages.formatKey(node.getGroupKey())))
                .tag("position", Tag.inserting(Messages.formatVector(node.getPosition())))
                .tag("curve-length", Tag.preProcessParsed(node.getBezierTangentLength() + ""))
                .tag("edge-count", Tag.preProcessParsed(node.getEdges().size() + ""))
                .build());

        TranslationHandler.getInstance().sendMessage(message, player);
    }

    @Subcommand("create default")
    @Syntax("<name>")
    @CommandPermission("pathfinder.command.waypoint.create")
    public void onCreate(Player player, @Single String name) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
        Node node = roadMap.createNode(player.getLocation().toVector().add(new Vector(0, 1, 0)), name);
        //TODO save to database obvsly
        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE.format(TagResolver.resolver("name", Tag.inserting(node.getDisplayName()))), player);
    }

    @Subcommand("delete")
    @Syntax("<nodes>")
    @CommandPermission("pathfinder.command.waypoint.delete")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
    public void onDelete(Player player, NodeSelection selection) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
        selection.forEach(roadMap::removeNode);
        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE.format(TagResolver.resolver("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))), player);
    }

    @Subcommand("tphere")
    @Syntax("<nodes>")
    @CommandPermission("pathfinder.command.waypoint.tphere")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
    public void onTphere(Player player, NodeSelection selection) {
        selection.forEach(node -> node.setPosition(player.getLocation().toVector()));
        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
                .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                .tag("position", Tag.inserting(Messages.formatVector(player.getLocation().toVector())))
                .build()), player);
    }

    @Subcommand("tp")
    @Syntax("<nodes> <x> <y> <z>")
    @CommandPermission("pathfinder.command.waypoint.tp")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " 0|~ 0|~ 0|~")
    public void onTp(Player player, NodeSelection selection, String xS, String yS, String zS) {

        selection.forEach(node -> {
            double x = xS.startsWith("~") ? Double.parseDouble(xS.substring(1)) + node.getPosition().getX() : Double.parseDouble(xS);
            double y = xS.startsWith("~") ? Double.parseDouble(yS.substring(1)) + node.getPosition().getY() : Double.parseDouble(yS);
            double z = xS.startsWith("~") ? Double.parseDouble(zS.substring(1)) + node.getPosition().getZ() : Double.parseDouble(zS);
            Vector v = new Vector(x, y, z);
            node.setPosition(v);
        });

        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
                .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                .tag("position", Tag.inserting(Messages.formatVector(xS, yS, zS)))
                .build()), player);
    }

    @Subcommand("list")
    @Syntax("[<page>]")
    @CommandPermission("pathfinder.command.waypoint.list")
    public void onList(Player player, @Optional Integer pageInput) {
        pageInput = pageInput == null ? 0 : pageInput;
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        TagResolver resolver = TagResolver.builder()
                .tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
                .tag("page", Tag.preProcessParsed(pageInput + ""))
                .build();

        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_HEADER.format(resolver), player);

        PathPlugin.getInstance().getAudiences().player(player).sendMessage(Component.join(
                JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                CommandUtils.subList(new ArrayList<>(roadMap.getNodes()), pageInput, 40).stream()
                        .map(n -> {

                            TagResolver r = TagResolver.builder()
                                    .tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
                                    .tag("name", Tag.inserting(n.getDisplayName()))
                                    .tag("permission", Tag.preProcessParsed(n.getPermission() == null ? "null" : n.getPermission()))
                                    .tag("position", Tag.inserting(Messages.formatVector(n.getPosition())))
                                    .tag("group-key", Tag.preProcessParsed(n.getGroupKey() == null ? "null" : n.getGroupKey().toString()))
                                    .build();

                            return TranslationHandler.getInstance().translateLine(Messages.CMD_N_LIST_ELEMENT.format(resolver, r), player);
                        })
                        .toList()));
        TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_FOOTER.format(resolver), player);
    }

    @Subcommand("connect")
    @Syntax("<node> <node>")
    @CommandPermission("pathfinder.command.waypoint.connect")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " " + PathPlugin.COMPLETE_NODE_SELECTION)
    public void onConnect(Player player, NodeSelection startSelection, NodeSelection endSelection) {

        for (Node start : startSelection) {
            for (Node end : endSelection) {
                TagResolver resolver = TagResolver.builder()
                        .tag("start", Tag.inserting(start.getDisplayName()))
                        .tag("end", Tag.inserting(end.getDisplayName()))
                        .build();

                if (start.equals(end)) {
                    TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_IDENTICAL.format(resolver), player);
                    continue;
                }
                if (start.getEdges().stream().anyMatch(edge -> edge.getEnd().equals(end))) {
                    TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_ALREADY_CONNECTED.format(resolver), player);
                    continue;
                }
                start.connect(end);
                TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT.format(resolver), player);
            }
        }
    }

    @Subcommand("disconnect")
    @Syntax("<node> <node>")
    @CommandPermission("pathfinder.command.waypoint.disconnect")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " " + PathPlugin.COMPLETE_FINDABLES_CONNECTED)
    public void onDisconnect(Player player, NodeSelection startSelection, NodeSelection endSelection) {

        for (Node start : startSelection) {
            for (Node end : endSelection) {
                TagResolver resolver = TagResolver.builder()
                        .tag("start", Tag.inserting(start.getDisplayName()))
                        .tag("end", Tag.inserting(end.getDisplayName()))
                        .build();

                start.disconnect(end);
                TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DISCONNECT.format(resolver), player);
            }
        }
    }

    @Subcommand("set")
    public class WaypointSetCommand extends BaseCommand {

        @Subcommand("name")
        @Syntax("<nodes> <name>")
        @CommandPermission("pathfinder.command.waypoint.rename")
        @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
        public void onRename(Player player, NodeSelection selection, String name) {
            selection.forEach(node -> node.setNameFormat(name));
            TranslationHandler.getInstance().sendMessage(Messages.CMD_N_RENAMED.format(TagResolver.builder()
                    .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                    .tag("name", Tag.inserting(PathPlugin.getInstance().getMiniMessage().deserialize(name)))
                    .build()), player);
        }

        @Subcommand("permission")
        @Syntax("<nodes> <permission>")
        @CommandPermission("pathfinder.command.waypoint.setpermission")
        @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " null|some.custom.permission")
        public void onSetPermission(Player player, NodeSelection selection, @Single String perm) {
            selection.forEach(node -> node.setPermission(perm.equalsIgnoreCase("null") ? null : perm));
            TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_PERMISSION.format(TagResolver.builder()
                    .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                    .tag("permission", Tag.inserting(Component.text(perm)))
                    .build()), player);
        }

        @Subcommand("curve-length")
        @Syntax("<nodes> <length>")
        @CommandPermission("pathfinder.command.waypoint.setcurvelength")
        @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
        public void onSetTangent(Player player, NodeSelection selection, Double strength) {
            selection.forEach(node -> node.setBezierTangentLength(strength));
            TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_TANGENT.format(TagResolver.builder()
                    .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                    .tag("length", Tag.inserting(Component.text(strength)))
                    .build()), player);
        }

        @Subcommand("group")
        @Syntax("<nodes> <group>")
        @CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " null|" + PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
        public void onSetGroup(CommandSender sender, NodeSelection selection, @Single NamespacedKey key) {
            RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

            TagResolver resolver = TagResolver.builder()
                    .tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
                    .tag("group", Tag.inserting(Messages.formatKey(key)))
                    .build();

            NodeGroup group = roadMap.getNodeGroup(key);
            if (group == null) {
                TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP_UNKNOWN.format(resolver), sender);
                return;
            }
            selection.forEach(node -> node.setGroupKey(key));
            TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP.format(resolver), sender);
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);
        }
    }
}
