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

    @Subcommand("help")
    @CommandPermission("bcrew.command.roadmap.help")
    @Default
    public void onHelp(Player player) {
        String help = "\n" + ChatColor.DARK_GRAY + "===] "+ ChatColor.WHITE + ChatColor.UNDERLINE +
                "Roadmap Befehle" + ChatColor.DARK_GRAY + " [===";
        help += "\n" + ChatColor.GRAY + " - /rm create <Name> [Welt] [entdeckbare Karte]" + ChatColor.DARK_GRAY + " - " +
                ChatColor.GRAY + "erstelle eine Straßenkarte";

        //TODO dynamisches system oder fertig hardcoden
        PlayerUtils.sendMessage(player, help);

    }


    @Subcommand("create")
    @Syntax("<Name> [Welt] [entdeckbare Karte]")
    @CommandPermission("bcrew.command.roadmap.create")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onCreate(Player player, String name,
                         @Optional @Values("@worlds") World world,
                         @Optional boolean findableNodes) {

        if(world == null) {
            world = player.getWorld();
        }

        RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
        if(roadMap != null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Roadmap + " + ChatColor.GREEN + name + ChatColor.GRAY + " erfolgreich erstellt");
        } else {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Erstellen der Roadmap");
        }
    }

    @Subcommand("delete")
    @Syntax("<Straßenkarte>")
    @CommandPermission("bcrew.command.roadmap.delete")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onDelete(Player player, RoadMap roadMap) {

        if(RoadMapHandler.getInstance().deleteRoadMap(roadMap)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte " + ChatColor.GREEN + roadMap.getName() +
                    ChatColor.GRAY + " erfolgreich gelöscht.");
        } else {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Löschen der Straßenkarte: "
                    + roadMap + ".");
        }
    }

    @Subcommand("editmode")
    @Syntax("<Straßenkarte>")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    @CommandPermission("bcrew.command.roadmap.editmode")
    public void onEdit(Player player, RoadMap roadMap) {

        roadMap.toggleEditMode(player);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Bearbeitungsmodus: " +
                (roadMap.isEditing(player) ? "AKTIVIERT" : "DEAKTIVIERT"));
    }

    @Subcommand("list")
    @CommandPermission("bcrew.command.roadmap.list")
    public void onList(Player player) {

        String list = "\n" + ChatColor.DARK_GRAY + "===] "+ ChatColor.WHITE + ChatColor.UNDERLINE +
                "Roadmaps" + ChatColor.DARK_GRAY + " [===";

        for(RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {

            String isEditing = "";
            if(roadMap.isEditing(player)) {
                isEditing = ChatColor.GREEN + " BEARBEITUNGSMODUS";
            }

            list += "\n" + ChatColor.GRAY + " - " + ChatColor.GREEN + roadMap.getName() +
                    ChatColor.DARK_GRAY + "(ID: " + roadMap.getDatabaseId() + ") " + ChatColor.GRAY + "Welt: " +
                    roadMap.getWorld().getName() + isEditing;
        }
        PlayerUtils.sendMessage(player, list);
    }

    @Subcommand("style")
    @Syntax("<Style>")
    @CommandPermission("bcrew.command.roadmap.style")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_VISUALIZER)
    public void onStyle(Player player, RoadMap roadMap, PathVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-m    Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("rename")
    @Syntax("<Straßenkarte> <neuer Name>")
    @CommandPermission("bcrew.command.roadmap.rename")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onRename(Player player, RoadMap roadMap, @Single String nameNew) {
        String nameOld = roadMap.getName();

        roadMap.setName(nameNew);
        if(nameOld.equals(roadMap.getName())) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben.");
        } else {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte erfolgreich umbenannt: " + nameNew);
        }
    }

    @Subcommand("changeworld")
    @Syntax("<Straßenkarte> <Welt> [erzwingen]")
    @CommandPermission("bcrew.command.roadmap.changeworld")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onChangeWorld(Player player, RoadMap roadMap, World world, boolean force) {

        if(force || !roadMap.isEdited()) {

            if(roadMap.isEdited()) {
                roadMap.cancelAllEditModes();
            }
            roadMap.setWorld(world);
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Die Welt für " + roadMap.getName() + " erfolgreich gewechselt.\n" +
                    ChatColor.RED + "ACHTUNG! Wegpunke sind möglicherweise nicht da, wo man sie erwartet.");
        } else {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Diese Straßenkarte wird gerade bearbeitet. " +
                    "Nutze den [erzwingen] Parameter, um die Welt dennoch zu verwenden.");
        }
    }
}
