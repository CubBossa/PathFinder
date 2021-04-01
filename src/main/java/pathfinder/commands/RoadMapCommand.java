package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pathfinder.Node;
import pathfinder.PathPlayer;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;
import pathfinder.visualisation.EditModeVisualizer;
import pathfinder.visualisation.PathVisualizer;

import java.awt.*;

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

    @Subcommand("info")
    @Syntax("<Straßenkarte>")
    @CommandPermission("bcrew.command.roadmap.info")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onInfo(Player player, RoadMap roadMap) {

        //TODO

    }


    @Subcommand("create")
    @Syntax("<Name> [<Welt>] [findbar]")
    @CommandPermission("bcrew.command.roadmap.create")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " findbar")
    public void onCreate(Player player, String name,
                         @Optional @Values("@worlds") World world,
                         @Optional @Single @Values("findbar") String findable) {

        boolean findableNodes = findable != null;
        if(world == null) {
            world = player.getWorld();
        }

        RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
        if(roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Erstellen der Roadmap");
            return;
        }

        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Roadmap + " + ChatColor.GREEN + name + ChatColor.GRAY + " erfolgreich erstellt");
    }

    @Subcommand("delete")
    @Syntax("<Straßenkarte>")
    @CommandPermission("bcrew.command.roadmap.delete")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onDelete(Player player, RoadMap roadMap) {

        if (!RoadMapHandler.getInstance().deleteRoadMap(roadMap)) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Löschen der Straßenkarte: "
                    + roadMap + ".");
            return;
        }

        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte " + ChatColor.GREEN + roadMap.getName() +
                ChatColor.GRAY + " erfolgreich gelöscht.");
    }

    @Subcommand("editmode")
    @Syntax("<Straßenkarte>")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    @CommandPermission("bcrew.command.roadmap.editmode")
    public void onEdit(Player player, RoadMap roadMap) {

        roadMap.toggleEditMode(player.getUniqueId());
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
    @Syntax("<Straßenkarte> path|editmode <Style>")
    @CommandPermission("bcrew.command.roadmap.style.path")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " path " + PathPlugin.COMPLETE_VISUALIZER)
    public void onStyle(Player player, RoadMap roadMap, PathVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-m    Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("style")
    @Syntax("<Straßenkarte> path|editmode <Style>")
    @CommandPermission("bcrew.command.roadmap.style.editmode")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " editmode " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onStyleEditMode(Player player, RoadMap roadMap, EditModeVisualizer visualizer) {

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
            return;
        }

        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte erfolgreich umbenannt: " + nameNew);
    }

    @Subcommand("tangent-strength")
    @Syntax("<Wert>")
    @CommandPermission("bcrew.command.roadmap.tangentstrength")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onChangeTangentStrength(CommandSender sender, RoadMap roadMap, double strength) {
        roadMap.setDefaultBezierTangentLength(strength);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Standard-Tangentenstärke erfolgreich gesetzt: " + strength);
    }

    @Subcommand("changeworld")
    @Syntax("<Straßenkarte> <Welt> [erzwingen]")
    @CommandPermission("bcrew.command.roadmap.changeworld")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " erzwingen")
    public void onChangeWorld(Player player, RoadMap roadMap, World world, @Optional @Single @Values("erzwingen") String forceString) {
        boolean force = forceString != null;

        if(!force && roadMap.isEdited()) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Diese Straßenkarte wird gerade bearbeitet. " +
                    "Nutze den [erzwingen] Parameter, um die Welt dennoch zu verwenden.");
            return;
        }

        if(roadMap.isEdited()) {
            roadMap.cancelEditModes();
        }

        roadMap.setWorld(world);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Die Welt für " + roadMap.getName() + " wurde erfolgreich gewechselt.\n" +
                    ChatColor.RED + "ACHTUNG! Wegpunke sind möglicherweise nicht da, wo man sie erwartet.");
    }

    @Subcommand("forcefind")
    @Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
    @CommandPermission("bcrew.command.roadmap.forcefind")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS +
            " @nodes ungruppiert") //TODO nodenamen als completion per map definieren
    public void onForceFind(Player player, RoadMap roadMap, Player target, String nodename,
                            @Optional @Single @Values("ungruppiert") String ungrouped) {

        boolean findSingle = ungrouped != null;
        PathPlayer pathPlayer = PlayerHandler.getInstance().getPlayer(target.getUniqueId());
        assert pathPlayer != null;

        boolean all = nodename.equals("*");
        for(Node n : roadMap.getNodes()) {
            if(n.getName().equals(nodename) || all) {
                pathPlayer.findNode(n, !findSingle);
                if(!all) return;
            }
        }
    }

    @Subcommand("forceforget")
    @Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
    @CommandPermission("bcrew.command.roadmap.forceforget")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS +
            " @nodes ungruppiert") //TODO nodenamen als completion per map definieren
    public void onForceForget(Player player, RoadMap roadMap, Player target, String nodename,
                              @Optional @Single @Values("ungruppiert") String ungrouped) {

        boolean findSingle = ungrouped != null;
        PathPlayer pathPlayer = PlayerHandler.getInstance().getPlayer(target.getUniqueId());
        assert pathPlayer != null;

        boolean all = nodename.equals("*");
        for(Node n : roadMap.getNodes()) {
            if(n.getName().equals(nodename) || all) {
                pathPlayer.unfindNode(n, !findSingle);
                if(!all) return;
            }
        }
    }

    @Subcommand("set-findable")
    @Syntax("<Straßenkarte> <findbare Nodes>")
    @CommandPermission("bcrew.command.roadmap.set-findable")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onSetFindable(CommandSender sender, RoadMap roadMap, Boolean findbar) {
        roadMap.setFindableNodes(findbar);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Findbarkeit umgestellt auf: " + ChatColor.GREEN + (findbar ? "an" : "aus"));
    }

    @Subcommand("set-find-distance")
    @Syntax("<Straßenkarte> <finde-entfernung>")
    @CommandPermission("bcrew.command.roadmap.set-find-distance")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onFindDistance(CommandSender sender, RoadMap roadMap, double findDistance) {
        //TODO nur positive doubles
        roadMap.setNodeFindDistance(findDistance);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Finde-Entfernung erfolgreich gesetzt: " + ChatColor.GREEN + findDistance);
    }

}
