package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;
import pathfinder.handler.RoadMapHandler;
import pathfinder.visualisation.PathVisualizer;

@CommandAlias("roadmap|rm")
public class RoadMapCommand extends BaseCommand {

    @Subcommand("create")
    @Syntax("[name] <Welt> <entdeckbare Karte>")
    @CommandPermission("bcrew.command.roadmap.create")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onCreate(Player player, String name,
                         @Optional @Values("@worlds") World world,
                         @Optional boolean findableNodes) {

        if(world == null) {
            world = player.getWorld();
        }

        RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
        //TODO message: roadmap erstellt
    }

    @Subcommand("delete")
    @Syntax("[name]")
    @CommandPermission("bcrew.command.roadmap.delete")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onDelete(Player player, RoadMap roadMap) {

        if(RoadMapHandler.getInstance().deleteRoadMap(roadMap)) {
            //TODO message erfolgreich gelöscht
            PlayerUtils.sendMessage(player, ChatColor.GREEN + "Straßenkarte " + roadMap.getName() + " erfolgreich gelöscht."); //TODO prefix
        } else {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Fehler beim Löschen der Straßenkarte: " + roadMap + ".");
        }
    }

    @Subcommand("editmode")
    @Syntax("[Name]")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    @CommandPermission("bcrew.command.roadmap.editmode")
    public void onEdit(Player player, RoadMap roadMap) {

        roadMap.toggleEditMode(player);
    }

    @Subcommand("list")
    @CommandPermission("bcrew.command.roadmap.list")
    @Default
    public void onList(Player player) {
        //TODO schicke eine Liste aller bestehenden Roadmaps
    }

    @Subcommand("style")
    @Syntax("[visualizer]")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_VISUALIZER)
    public void onStyle(Player player, RoadMap roadMap, PathVisualizer visualizer) { //TODO pathVisualizer Component
        //TODO ein visualizer für genannte straßenkarte registrieren
    }
}
