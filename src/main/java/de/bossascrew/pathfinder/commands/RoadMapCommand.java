package de.bossascrew.pathfinder.commands;

import com.google.common.collect.Lists;
import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
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

@CommandAlias("roadmap")
public class RoadMapCommand extends BaseCommand {

    @Subcommand("help")
    @CatchUnknown
    @CommandPermission("bcrew.command.roadmap.help")
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
    public void onInfo(CommandSender sender, RoadMap roadMap) {

        Menu menu = new Menu(ChatColor.DARK_GRAY + "===] " + ChatColor.WHITE + ChatColor.UNDERLINE + roadMap.getName() + ChatColor.DARK_GRAY + " [===");
        menu.addSub(getSubMenu(
                Component.text("Name: ").append(Component.text(roadMap.getName(), NamedTextColor.GREEN)),
                Component.text("Klicke, um den Namen zu ändern."),
                "/roadmap rename " + roadMap.getName() + " <neuer Name>"));
        menu.addSub(getSubMenu(
                Component.text("Welt: ").append(Component.text(roadMap.getWorld().getName(), NamedTextColor.GREEN)),
                Component.text("Klicke, um die Welt zu ändern."),
                "/roadmap setworld " + roadMap.getName() + " <Welt>"));
        menu.addSub(getSubMenu(
                Component.text("Pfadvisualisierer: ").append(Component.text(roadMap.getVisualizer().getName(), NamedTextColor.GREEN)),
                Component.text("Klicke, um den Partikelstyle zu wechseln"),
                "/roadmap style " + roadMap.getName() + " path <Style>"));
        menu.addSub(getSubMenu(
                Component.text("Editmode-Visualisierer: ").append(Component.text(roadMap.getEditModeVisualizer().getName(), NamedTextColor.GREEN)),
                Component.text("Klicke, um den Editmode-Partikelstyle zu wechseln"),
                "/roadmap style " + roadMap.getName() + " editmode <Style>"));

        PlayerUtils.sendComponents(sender, menu.toComponents());
    }

    private ComponentMenu getSubMenu(Component text, Component hover, String command) {
        return new ComponentMenu(text
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.suggestCommand(command)));
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

        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Roadmap " + ChatColor.GREEN + name + ChatColor.GRAY + " erfolgreich erstellt");
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

        Component list = Component.text("===] ", NamedTextColor.DARK_GRAY)
                .append(Component.text("Roadmaps").color(NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, true))
                .append(Component.text(" [===", NamedTextColor.DARK_GRAY));

        for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {

            String isEditing = "";
            if (sender instanceof Player) {
                if (roadMap.isEditing((Player) sender)) {
                    isEditing = ChatColor.GREEN + " BEARBEITUNGSMODUS";
                }
            }

            list = list.append(Component.newline())
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(roadMap.getName() + "(#" + roadMap.getDatabaseId() + ")", NamedTextColor.DARK_GREEN))
                    .append(Component.text(", Welt: ", NamedTextColor.GRAY))
                    .append(Component.text(roadMap.getWorld().getName(), NamedTextColor.GREEN)
                            .hoverEvent(HoverEvent.showText(Component.text("Klicke zum Teleportieren")))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/world " + roadMap.getWorld().getName()))) //TODO zu erster node teleportieren
                    .append(Component.text(isEditing, NamedTextColor.RED));
        }
        PlayerUtils.sendComponents(sender, Lists.newArrayList(list));
    }

    @Subcommand("set path-visualizer")
    @Syntax("<Straßenkarte> <Style>")
    @CommandPermission("bcrew.command.roadmap.set.path-visualizer")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onStyle(Player player, RoadMap roadMap, PathVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("set editmode-visualizer")
    @Syntax("<Straßenkarte> <Style>")
    @CommandPermission("bcrew.command.roadmap.set.editmode-visualizer")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onStyleEditMode(Player player, RoadMap roadMap, EditModeVisualizer visualizer) {

        roadMap.setVisualizer(visualizer);
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
    }

    @Subcommand("set name")
    @Syntax("<Straßenkarte> <neuer Name>")
    @CommandPermission("bcrew.command.roadmap.set.name")
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

    @Subcommand("set world")
    @Syntax("<Straßenkarte> <Welt> [erzwingen]")
    @CommandPermission("bcrew.command.roadmap.set.world")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " erzwingen")
    public void onChangeWorld(CommandSender sender, RoadMap roadMap, World world, @Optional @Single @Values("erzwingen") String forceString) {
        boolean force = forceString != null;

        if (!force && roadMap.isEdited()) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Diese Straßenkarte wird gerade bearbeitet. " +
                    "Nutze den [erzwingen] Parameter, um die Welt trotzdem zu ändern.");
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
        if (pathPlayer == null) {
            return;
        }

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
        if (pathPlayer == null) {
            return;
        }

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

    @Subcommand("set findable")
    @Syntax("<Straßenkarte> <findbare Nodes>")
    @CommandPermission("bcrew.command.roadmap.set.findable")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_BOOLEAN)
    public void onSetFindable(CommandSender sender, RoadMap roadMap, Boolean findbar) {
        roadMap.setFindableNodes(findbar);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Findbarkeit umgestellt auf: " + ChatColor.GREEN + (findbar ? "an" : "aus"));
    }

    @Subcommand("set find-distance")
    @Syntax("<Straßenkarte> <finde-entfernung>")
    @CommandPermission("bcrew.command.roadmap.set.find-distance")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onFindDistance(CommandSender sender, RoadMap roadMap, double findDistance) {
        if (findDistance < 0.05) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Die angebenene Distanz ist zu klein.");
            return;
        }
        roadMap.setNodeFindDistance(findDistance);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Finde-Entfernung erfolgreich gesetzt: " + ChatColor.GREEN + findDistance);
    }

    @Subcommand("select")
    @Syntax("<Straßenkarte>")
    @CommandPermission("bcrew.command.roadmap.select")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onSelect(Player player, RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if(pathPlayer == null) {
            return;
        }
        pathPlayer.setSelectedRoadMap(roadMap.getDatabaseId());
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte ausgewählt: " + roadMap.getName());
    }

    @Subcommand("deselect")
    @CommandPermission("bcrew.command.roadmap.select")
    public void onDeselect(Player player) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if(pathPlayer == null) {
            return;
        }
        pathPlayer.deselectRoadMap();
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte nicht mehr ausgewählt.");
    }
}
