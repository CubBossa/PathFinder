package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.NodeGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("nodegroup|ng|findablegroup")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @Syntax("[<Seite>]")
    @CommandPermission("pathfinder.command.nodegroup.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        Menu menu = new Menu(PathPlugin.CHAT_COLOR_DARK + "Gruppen für " + roadMap.getNameFormat());

        for (NodeGroup group : roadMap.getGroups().values()) {
            Component entry = Component.text(group.getNameFormat() + " (#" + group.getGroupId() + ")", PathPlugin.COLOR_LIGHT)
                    .append(Component.text(", Größe: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getFindables().size(), PathPlugin.COLOR_LIGHT));
            menu.addSub(new ComponentMenu(entry));
        }
        PlayerUtils.sendComponents(player, menu.toComponents());
    }

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("pathfinder.command.nodegroup.create")
    public void onCreate(Player player, String name) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        if (!roadMap.isGroupNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        //TODO wirft exception
        roadMap.addFindableGroup(name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich erstellt: " + PathPlugin.CHAT_COLOR_LIGHT + name);
    }

    @Subcommand("delete")
    @Syntax("<Gruppe>")
    @CommandPermission("pathfinder.command.nodegroup.delete")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
    public void onDelete(Player player, NodeGroup group) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        roadMap.deleteFindableGroup(group);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich gelöscht: " + PathPlugin.CHAT_COLOR_LIGHT + group.getNameFormat());
    }

    @Subcommand("set name")
    @Syntax("<Gruppe> <neuer Name>")
    @CommandPermission("pathfinder.command.nodegroup.rename")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
    public void onRename(Player player, NodeGroup group, @Single String newName) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        if (!roadMap.isGroupNameUnique(newName)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        group.setNameFormat(newName, true);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich umbenannt: " + PathPlugin.CHAT_COLOR_LIGHT + newName);
    }

    @Subcommand("set findable")
    @Syntax("<Gruppe> <findbar>")
    @CommandPermission("pathfinder.command.nodegroup.setfindable")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onSetFindable(Player player, NodeGroup group, boolean findable) {
        group.setFindable(findable, true);
        if(group.getRoadMap().isEdited()) {
            for (Waypoint f : group.getFindables()) {
                group.getRoadMap().updateArmorStandDisplay(f, false);
            }
        }
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Die Findbarkeit geändert auf: " + PathPlugin.CHAT_COLOR_LIGHT + (findable ? "an" : "aus"));
    }
}
