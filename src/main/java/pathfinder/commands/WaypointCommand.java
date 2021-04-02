package pathfinder.commands;

import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pathfinder.Node;
import pathfinder.PathPlayer;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;

@CommandAlias("waypoint|wp")
public class WaypointCommand {

    @Subcommand("create")
    @Syntax("<Name>")
    @CommandPermission("bcrew.command.waypoint.create")
    public void onCreate(Player player, @Single String name) {
        PathPlayer pplayer = PlayerHandler.getInstance().getPlayer(player.getUniqueId());
        assert pplayer != null;
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMapId());
        if(roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Du musst eine RoadMap auswählen. (/roadmap select)");
            return;
        }
        if(!roadMap.isNodeNameUnique(name)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben.");
            return;
        }
        roadMap.createNode(player.getLocation().toVector(), name);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich erstellt: " + ChatColor.GREEN + name);
    }

    @Subcommand("delete")
    @Syntax("<Node>")
    @CommandPermission("bcrew.command.waypoint.delete")
    public void onDelete(Player player, @Single String nodeName) {
        PathPlayer pplayer = PlayerHandler.getInstance().getPlayer(player.getUniqueId());
        assert pplayer != null;
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMapId());
        if(roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Du musst eine RoadMap auswählen. (/roadmap select)");
            return;
        }
        Node node = roadMap.getNode(nodeName);
        if(node == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Diese Node existiert nicht.");
            return;
        }
        roadMap.deleteNode(node);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Node erfolgreich gelöscht: " + ChatColor.GREEN + node.getName());
    }





    //TODO
    //tphere
    //permission
    //rename
    //tangentstrength
    //group clear
    //group set
    //connect [node] [node]



}
