package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.util.PagedChatMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("nodegroup|ng")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @Syntax("[<Seite>]")
    @CommandPermission("bcrew.command.nodegroup.list")
    public void onList(Player player, @Optional Integer pageInput) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        int page = 0;
        if (pageInput != null) {
            page = pageInput - 1;
        }

        Component title = Component.text("===] ", NamedTextColor.DARK_GRAY)
                .append(Component.text(roadMap.getName() + "-Gruppen", NamedTextColor.WHITE)
                        .decoration(TextDecoration.UNDERLINED, true))
                .append(Component.text(" [==", NamedTextColor.DARK_GRAY));

        PagedChatMenu menu = new PagedChatMenu(title, 10, "nodegroup list %PAGE%");

        //outofbounds verhindern
        if (page >= menu.getPageCount()) {
            page = menu.getPageCount() - 1;
        }
        if (page < 1) {
            page = 1;
        }

        for (FindableGroup group : roadMap.getGroups()) {
            Component entry = Component.text(group.getName(), NamedTextColor.GREEN)
                    .append(getSeperator()).append(Component.text("ID: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getDatabaseId(), NamedTextColor.DARK_GREEN))
                    .append(getSeperator()).append(Component.text("Größe: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getFindables().size(), NamedTextColor.DARK_GREEN));
            menu.addEntry(entry);
        }
        player.sendMessage(menu.getPage(page));
    }

    //TODO componentUtils?
    private Component getSeperator() {
        return Component.text(" | ", NamedTextColor.DARK_GRAY);
    }

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.nodegroup.create")
    public void onCreate(Player player, String name) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        if (!roadMap.isGroupNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        roadMap.addFindableGroup(name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Gruppe>")
    @CommandPermission("bcrew.command.nodegroup.delete")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_GROUPS)
    public void onDelete(Player player, FindableGroup group) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        roadMap.deleteFindableGroup(group.getDatabaseId());
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich gelöscht: " + ChatColor.GREEN + group.getName());
    }

    @Subcommand("rename")
    @Syntax("<Gruppe> <neuer Name>")
    @CommandPermission("bcrew.command.nodegroup.rename")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_GROUPS)
    public void onRename(Player player, FindableGroup group, @Single String newName) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        if (!roadMap.isGroupNameUnique(newName)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        group.setName(newName);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich umbenannt: " + ChatColor.GREEN + newName);
    }

    @Subcommand("setfindable")
    @Syntax("<Gruppe> <findbar>")
    @CommandPermission("bcrew.command.nodegroup.setfindable")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_GROUPS)
    public void onSetFindable(Player player, FindableGroup group, boolean findable) {
        group.setFindable(findable);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Die Findbarkeit geändert auf: " + ChatColor.GREEN + (findable ? "an" : "aus"));
    }

    //TODO
    //info

    private RoadMap getRoadMap(Player player) {
        PathPlayer pplayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        assert pplayer != null;
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMapId());
        if (roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Du musst eine RoadMap auswählen. (/roadmap select)");
            return null;
        }
        return roadMap;
    }
}
