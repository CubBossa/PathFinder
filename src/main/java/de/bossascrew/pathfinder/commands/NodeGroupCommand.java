package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("nodegroup|ng|findablegroup")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @Syntax("[<Seite>]")
    @CommandPermission("bcrew.command.nodegroup.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        Menu menu = new Menu(ChatColor.DARK_GREEN + "Gruppen für " + roadMap.getName());

        for (FindableGroup group : roadMap.getGroups().values()) {
            Component entry = Component.text(group.getName() + " (#" + group.getDatabaseId() + ")", NamedTextColor.GREEN)
                    .append(Component.text(", Größe: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getFindables().size(), NamedTextColor.GREEN));
            menu.addSub(new ComponentMenu(entry));
        }
        PlayerUtils.sendComponents(player, menu.toComponents());
    }

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.nodegroup.create")
    public void onCreate(Player player, String name) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        if (!roadMap.isGroupNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        //TODO wirft exception
        roadMap.addFindableGroup(name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Gruppe>")
    @CommandPermission("bcrew.command.nodegroup.delete")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS)
    public void onDelete(Player player, FindableGroup group) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        roadMap.deleteFindableGroup(group);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich gelöscht: " + ChatColor.GREEN + group.getName());
    }

    @Subcommand("set name")
    @Syntax("<Gruppe> <neuer Name>")
    @CommandPermission("bcrew.command.nodegroup.rename")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS)
    public void onRename(Player player, FindableGroup group, @Single String newName) {
        RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

        if (!roadMap.isGroupNameUnique(newName)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        group.setName(newName);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich umbenannt: " + ChatColor.GREEN + newName);
    }

    @Subcommand("set findable")
    @Syntax("<Gruppe> <findbar>")
    @CommandPermission("bcrew.command.nodegroup.setfindable")
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_GROUPS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onSetFindable(Player player, FindableGroup group, boolean findable) {
        group.setFindable(findable);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Die Findbarkeit geändert auf: " + ChatColor.GREEN + (findable ? "an" : "aus"));
    }
    //TODO
    //info
}
