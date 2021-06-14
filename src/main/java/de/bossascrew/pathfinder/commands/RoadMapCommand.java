package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.visualisation.PathVisualizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

@CommandAlias("roadmap|rm")
public class RoadMapCommand extends BaseCommand {

    @Subcommand("help")
    @CommandPermission("bcrew.command.roadmap.help")
    @Default
    public void onHelp(Player player) {
        String help = "\n" + ChatColor.DARK_GRAY + "===] " + ChatColor.WHITE + ChatColor.UNDERLINE +
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

        Component text = Component.text("===] ", NamedTextColor.DARK_GRAY)
                .append(Component.text(roadMap.getName(), NamedTextColor.WHITE)
                        .decoration(TextDecoration.UNDERLINED, true)
                        .append(Component.text(" [===", NamedTextColor.DARK_GRAY)))
                .append(getInfoEntry("Name: " + ChatColor.GREEN + roadMap.getName(),
                        "/roadmap rename " + roadMap.getName() + " <neuer Name>",
                        "Klicke, um den Namen zu ändern."))
                .append(getInfoEntry("Welt: " + ChatColor.GREEN + roadMap.getWorld(),
                        "/roadmap setworld " + roadMap.getName() + " <Name>",
                        "Klicke, um die Welt zu ändern."))
                .append(getInfoEntry("Pfadvisualisierer: " + ChatColor.GREEN + roadMap.getVisualizer().getName(),
                        "/roadmap style " + roadMap.getName() + " path <Style>",
                        "Klicke, um den Partikelstyle zu wechseln"))
                .append(getInfoEntry("Editmode-visualisierer: " + ChatColor.GREEN + roadMap.getEditModeVisualizer().getName(),
                        "/roadmap style " + roadMap.getName() + " editmode <Style>",
                        "Klicke, um den Editmode-Partikelstyle zu wechseln"));
    }

    private Component getInfoEntry(String name, String suggest, String hover) {
        Component text = ComponentUtils.translateLegacy(ChatColor.WHITE + "\n » " + name)
                .hoverEvent(HoverEvent.showText(ComponentUtils.translateLegacy(hover)))
                .clickEvent(ClickEvent.suggestCommand(suggest));
        return text;
    }


    @Subcommand("create")
    @Syntax("<Name> [<Welt>] [findbar]")
    @CommandPermission("bcrew.command.roadmap.create")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " findbar")
    public void onCreate(Player player, String name,
                         @Optional @Values("@worlds") World world,
                         @Optional @Single @Values("findbar") String findable) {

        boolean findableNodes = findable != null;
        if (world == null) {
            world = player.getWorld();
        }

        RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
        if (roadMap == null) {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Erstellen der Roadmap");
            return;
        }

        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Roadmap + " + ChatColor.GREEN + name + ChatColor.GRAY + " erfolgreich erstellt");
    }

    @Subcommand("delete")
    @Syntax("<Straßenkarte>")
    @CommandPermission("bcrew.command.roadmap.delete")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onDelete(CommandSender sender, RoadMap roadMap) {

        RoadMapHandler.getInstance().deleteRoadMap(roadMap);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte " + ChatColor.GREEN + roadMap.getName() +
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
    public void onList(CommandSender sender) {

        String list = "\n" + ChatColor.DARK_GRAY + "===] " + ChatColor.WHITE + ChatColor.UNDERLINE +
                "Roadmaps" + ChatColor.DARK_GRAY + " [===";

        for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {

            String isEditing = "";
            if (sender instanceof Player) {
                if (roadMap.isEditing((Player) sender)) {
                    isEditing = ChatColor.GREEN + " BEARBEITUNGSMODUS";
                }
            }

            list += "\n" + ChatColor.GRAY + " - " + ChatColor.GREEN + roadMap.getName() +
                    ChatColor.DARK_GRAY + "(ID: " + roadMap.getDatabaseId() + ") " + ChatColor.GRAY + "Welt: " +
                    roadMap.getWorld().getName() + isEditing;
        }
        PlayerUtils.sendMessage(sender, list);
    }

    @Subcommand("style")
    @Syntax("<Straßenkarte> path|editmode <Style>")
    @CommandPermission("bcrew.command.roadmap.style.path")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " path " + PathPlugin.COMPLETE_VISUALIZER)
    public void onStyle(Player player, RoadMap roadMap, String pathmode, PathVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("style")
    @Syntax("<Straßenkarte> path|editmode <Style>")
    @CommandPermission("bcrew.command.roadmap.style.editmode")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " editmode " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onStyleEditMode(Player player, RoadMap roadMap, String editmode, EditModeVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("rename")
    @Syntax("<Straßenkarte> <neuer Name>")
    @CommandPermission("bcrew.command.roadmap.rename")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onRename(CommandSender sender, RoadMap roadMap, @Single String nameNew) {
        String nameOld = roadMap.getName();
        roadMap.setName(nameNew);

        if (nameOld.equals(roadMap.getName())) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben.");
            return;
        }

        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte erfolgreich umbenannt: " + nameNew);
    }

    @Subcommand("tangent-strength")
    @Syntax("<Wert>")
    @CommandPermission("bcrew.command.roadmap.tangentstrength")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onChangeTangentStrength(CommandSender sender, RoadMap roadMap, double strength) {
        roadMap.setDefaultBezierTangentLength(strength);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Standard-Tangentenstärke erfolgreich gesetzt: " + strength);
    }

    @Subcommand("setworld")
    @Syntax("<Straßenkarte> <Welt> [erzwingen]")
    @CommandPermission("bcrew.command.roadmap.setworld")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " erzwingen")
    public void onChangeWorld(CommandSender sender, RoadMap roadMap, World world, @Optional @Single @Values("erzwingen") String forceString) {
        boolean force = forceString != null;

        if (!force && roadMap.isEdited()) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Diese Straßenkarte wird gerade bearbeitet. " +
                    "Nutze den [erzwingen] Parameter, um die Welt dennoch zu verwenden.");
            return;
        }

        if (roadMap.isEdited()) {
            roadMap.cancelEditModes();
        }

        roadMap.setWorld(world);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Die Welt für " + roadMap.getName() + " wurde erfolgreich gewechselt.\n" +
                ChatColor.RED + "ACHTUNG! Wegpunke sind möglicherweise nicht da, wo man sie erwartet.");
    }

    @Subcommand("forcefind")
    @Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
    @CommandPermission("bcrew.command.roadmap.forcefind")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS +
            " @nodes ungruppiert") //TODO nodenamen als completion per map definieren
    public void onForceFind(CommandSender sender, RoadMap roadMap, Player target, String nodename,
                            @Optional @Single @Values("ungruppiert") String ungrouped) {

        boolean findSingle = ungrouped != null;
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
        assert pathPlayer != null;

        boolean all = nodename.equals("*");
        for (Findable findable : roadMap.getFindables()) {
            if (findable.getName().equals(nodename) || all) {
                pathPlayer.find(findable, !findSingle, new Date());
                if (!all) {
                    break;
                }
            }
        }
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Spieler " + ChatColor.GREEN + target.getName() +
                ChatColor.GRAY + " hat " + ChatColor.GREEN + nodename + ChatColor.GRAY + " gefunden.");
    }

    @Subcommand("forceforget")
    @Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
    @CommandPermission("bcrew.command.roadmap.forceforget")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS +
            " @nodes ungruppiert") //TODO nodenamen als completion per map definieren
    public void onForceForget(CommandSender sender, RoadMap roadMap, Player target, String nodename,
                              @Optional @Single @Values("ungruppiert") String ungrouped) {

        boolean findSingle = ungrouped != null;
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
        assert pathPlayer != null;

        boolean all = nodename.equals("*");
        for (Findable findable : roadMap.getFindables()) {
            if (findable.getName().equals(nodename) || all) {
                pathPlayer.unfind(findable, !findSingle);
                if (!all) {
                    break;
                }
            }
        }
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Spieler " + ChatColor.GREEN + target.getName() +
                ChatColor.GRAY + " hat " + ChatColor.GREEN + nodename + ChatColor.GRAY + " vergessen.");
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
        if(findDistance < 0.05) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Die angebenene Distanz ist zu klein.");
            return;
        }
        roadMap.setNodeFindDistance(findDistance);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Finde-Entfernung erfolgreich gesetzt: " + ChatColor.GREEN + findDistance);
    }

}
