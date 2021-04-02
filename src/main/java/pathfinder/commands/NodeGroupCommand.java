package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pathfinder.NodeGroup;
import pathfinder.PathPlayer;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;

@CommandAlias("nodegroup|ng")
public class NodeGroupCommand extends BaseCommand {

    @Subcommand("list")
    @CommandPermission("bcrew.command.nodegroup.list")
    public void onList(Player player) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        Component list = Component.text("===] ", NamedTextColor.DARK_GRAY)
                .append(Component.text(roadMap.getName() + "-Gruppen", NamedTextColor.WHITE)
                    .decoration(TextDecoration.UNDERLINED, true))
                .append(Component.text(" [==", NamedTextColor.DARK_GRAY));
        for(NodeGroup group : roadMap.getGroups()) {
            list.append(Component.text("\n » ", NamedTextColor.WHITE)
                    .append(Component.text(group.getName(), NamedTextColor.GREEN))
                    .append(getSeperator()).append(Component.text("ID: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getDatabaseId(), NamedTextColor.DARK_GREEN))
                    .append(getSeperator()).append(Component.text("Größe: ", NamedTextColor.GRAY))
                    .append(Component.text(group.getNodes().size(), NamedTextColor.DARK_GREEN)));
        }
        player.sendMessage(list);
    }

    private Component getSeperator() {
        return Component.text(" | ", NamedTextColor.DARK_GRAY);
    }

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.nodegroup.create")
    public void onCreate(Player player, String name) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        if(!roadMap.isGroupNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        roadMap.addNodeGroup(name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Gruppe>")
    @CommandPermission("bcrew.command.nodegroup.delete")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_GROUPS)
    public void onDelete(Player player, NodeGroup group) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        roadMap.deleteNodeGroup(group.getDatabaseId());
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich gelöscht: " + ChatColor.GREEN + group.getName());
    }

    @Subcommand("rename")
    @Syntax("<Gruppe> <neuer Name>")
    @CommandPermission("bcrew.command.nodegroup.rename")
    @CommandCompletion(PathPlugin.COMPLETE_NODE_GROUPS)
    public void onRename(Player player, NodeGroup group, @Single String newName) {
        RoadMap roadMap = getRoadMap(player);
        assert roadMap != null;

        if(!roadMap.isGroupNameUnique(newName)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben");
            return;
        }
        group.setName(newName);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Gruppe erfolgreich umbenannt: " + ChatColor.GREEN + newName);
    }



    //TODO
    //info
    //setfindable

    private RoadMap getRoadMap(Player player) {
        PathPlayer pplayer = PlayerHandler.getInstance().getPlayer(player.getUniqueId());
        assert pplayer != null;
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMapId());
        if(roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Du musst eine RoadMap auswählen. (/roadmap select)");
            return null;
        }
        return roadMap;
    }
}
