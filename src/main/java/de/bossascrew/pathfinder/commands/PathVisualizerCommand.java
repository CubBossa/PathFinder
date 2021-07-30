package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;

/**
 * Command zum erstellen, löschen und bearbeiten von Pfadvisualizer-Profilen
 */
@CommandAlias("path-visualizer|pathv|pv")
public class PathVisualizerCommand extends BaseCommand {

    @Subcommand("list")
    @CommandPermission("bcrew.command.visualizer.path.list")
    public void onList(CommandSender sender) {

        ComponentMenu menu = new ComponentMenu(Component.text("Pfad-Visualizer").color(NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, true));

        for (PathVisualizer vis : VisualizerHandler.getInstance().getPathVisualizers()) {
            menu.addSub(new ComponentMenu(Component.text(vis.getName() + "(#" + vis.getDatabaseId() + ")", PathPlugin.COLOR_DARK)
					.append(Component.text(", Parent: ", NamedTextColor.GRAY))
					.append(vis.getParent() == null ?
							CommandUtils.NULL_COMPONENT :
							Component.text(vis.getParent().getName(), PathPlugin.COLOR_LIGHT))
					.clickEvent(ClickEvent.runCommand("/path-visualizer info " + vis.getName()))));
        }
        PlayerUtils.sendComponents(sender, menu.toComponents());
    }


    @Subcommand("create")
    @Syntax("<Name> <Partikel>")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_PARTICLES + " " + PathPlugin.COMPLETE_PATH_VISUALIZER)
    @CommandPermission("bcrew.command.visualizer.path.create")
    public void onCreate(CommandSender sender, @Single String name, @Optional Particle particle, @Optional String parent) {

        if (!VisualizerHandler.getInstance().isNameUniquePath(name)) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben");
            return;
        }
        PathVisualizer parentVis = VisualizerHandler.getInstance().getPathVisualizer(parent);
        if (parentVis == null) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Unbekannter Visualizer: \"" + parent + "\". Setze Default-Visualizer");
        }
        VisualizerHandler.getInstance().createPathVisualizer(name, parentVis, particle, null, null, null, null);

		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Pfad-Visualizer erstellt: " + PathPlugin.CHAT_COLOR_LIGHT + name);
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Nutze /path-visualizer " + name + " <Einstellung> <Wert>, um ihn zu bearbeiten");
    }

    @Subcommand("delete")
    @CommandPermission("bcrew.command.visualizer.path.delete")
    @Syntax("<Pfad-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onDelete(CommandSender sender, PathVisualizer visualizer) {
        if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Fehler beim Löschen von Pfad-Visualizer: " + visualizer.getName() + ".");
            return;
        }
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Visualizer erfolgreich gelöscht.");
    }

    @Subcommand("info")
    @CommandPermission("bcrew.command.visualizer.path.info")
    @Syntax("<Pfad-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onInfo(CommandSender sender, PathVisualizer visualizer) {

        ComponentMenu menu = new ComponentMenu(Component.text("Pfad-Visualizer: ", NamedTextColor.WHITE)
                .append(Component.text(visualizer.getName() + " (#" + visualizer.getDatabaseId() + ")")
						.color(PathPlugin.COLOR_DARK)
                        .hoverEvent(HoverEvent.showText(Component.text("Klicken zum Umbenennnen")))
                        .clickEvent(ClickEvent.suggestCommand("/path-visualizer set name " + visualizer.getName() + " <Neuer Name>"))));

        menu.addSub(new ComponentMenu(Component.text("Parent: ")
                .append(CommandUtils.getParentList(visualizer))
                .hoverEvent(HoverEvent.showText(Component.text("Parent setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set parent " + visualizer.getName() + " <Parent>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticle() == null ? null : Component.text(visualizer1.getUnsafeParticle().name(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle " + visualizer.getName() + " <Partikel>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Limit: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleLimit() == null ? null : Component.text(visualizer1.getUnsafeParticleLimit(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Limit setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-limit " + visualizer.getName() + " <Partikellimit>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Distanz: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleDistance() == null ? null : Component.text(visualizer1.getUnsafeParticleDistance(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-distance " + visualizer.getName() + " <Partikeldistanz>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Schritte: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleSteps() == null ? null : Component.text(visualizer1.getUnsafeParticleSteps(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-steps " + visualizer.getName() + " <Partikelschritte>"))));

        menu.addSub(new ComponentMenu(Component.text("Scheduler-Wiederholrate: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeSchedulerPeriod() == null ? null : Component.text(visualizer1.getUnsafeSchedulerPeriod(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Scheduler-Wiederholrate setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set scheduler-period " + visualizer.getName() + " <Wiederholrate in Ticks>"))));

        PlayerUtils.sendComponents(sender, menu.toComponents());
    }

    @Subcommand("set parent")
    @CommandPermission("bcrew.command.visualizer.path.set.parent")
    @Syntax("<Pfad-Visualizer> <Parent>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " " + PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onSetParent(CommandSender sender, PathVisualizer edit, PathVisualizer parent) {
        if (edit.getDatabaseId() == parent.getDatabaseId() || parent.hasParent(edit)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + parent.getName() + " ist ein ungültiger oder bereits gesetzter Parent.");
            return;
        }
        edit.setParent(parent);
		DatabaseModel.getInstance().updatePathVisualizer(edit);
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Parent aktuallisiert: " + parent.getName());
    }

    @Subcommand("set name")
    @CommandPermission("bcrew.command.visualizer.path.set.name")
    @Syntax("<Pfad-Visualizer> <Neuer Name>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onSetName(CommandSender sender, PathVisualizer edit, String newName) {
        newName = StringUtils.replaceSpaces(newName);
        edit.setAndSaveName(newName);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Name aktuallisiert: " + newName);
    }

    @Subcommand("set particle")
    @CommandPermission("bcrew.command.visualizer.path.set.particle")
    @Syntax("<Pfad-Visualizer> <Partikel>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " " + BukkitMain.COMPLETE_PARTICLES)
    public void onSetParticle(CommandSender sender, PathVisualizer edit, String particleName) {
        Particle particle = null;
        if (!particleName.equalsIgnoreCase("null")) {
            try {
                particle = Particle.valueOf(particleName);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Partikeleffekt: " + particleName);
                return;
            }
        }
        edit.setAndSaveParticle(particle);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikeleffekt aktuallisiert: " + (particle != null ? particle.name() : "null"));
    }

    @Subcommand("set particle-limit")
    @CommandPermission("bcrew.command.visualizer.path.set.particle-limit")
    @Syntax("<Pfad-Visualizer> <Limit>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER  + " null")
    public void onSetParticleLimit(CommandSender sender, PathVisualizer edit, @Single String limitString) {
        Integer limit = null;
        if (!limitString.equalsIgnoreCase("null")) {
            try {
                limit = Integer.parseInt(limitString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + limitString);
                return;
            }
        }
        edit.setAndSaveParticleLimit(limit);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikellimit aktuallisiert: " + limitString);
    }

    @Subcommand("set particle-distance")
    @CommandPermission("bcrew.command.visualizer.path.set.particle-distance")
    @Syntax("<Pfad-Visualizer> <Distanz>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER  + " null")
    public void onSetParticleDistance(CommandSender sender, PathVisualizer edit, @Single String distanceString) {
        Double distance = null;
        if (!distanceString.equalsIgnoreCase("null")) {
            try {
                distance = Double.parseDouble(distanceString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Double-Wert: " + distanceString);
                return;
            }
        }
        edit.setAndSaveParticleDistance(distance);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikeldistanz aktuallisiert: " + distanceString);
    }

    @Subcommand("set particle-steps")
    @CommandPermission("bcrew.command.visualizer.path.set.particle-steps")
    @Syntax("<Pfad-Visualizer> <Schritte>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
    public void onSetParticleSteps(CommandSender sender, PathVisualizer edit, @Single String stepString) {
        Integer steps = null;
        if (!stepString.equalsIgnoreCase("null")) {
            try {
                steps = Integer.parseInt(stepString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + stepString);
                return;
            }
        }
        edit.setAndSaveParticleSteps(steps);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikelschritte aktuallisiert: " + stepString);
    }

    @Subcommand("set scheduler-period")
    @CommandPermission("bcrew.command.visualizer.path.set.scheduler-period")
    @Syntax("<Pfad-Visualizer> <Scheduler-Wiederholabstand>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
    public void onSetSchedulerPeriod(CommandSender sender, PathVisualizer edit, @Single String schedulerPeriodString) {
        Integer schedulerPeriod = null;
        if (!schedulerPeriodString.equalsIgnoreCase("null")) {
            try {
                schedulerPeriod = Integer.parseInt(schedulerPeriodString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + schedulerPeriodString);
                return;
            }
        }
        edit.setAndSaveSchedulerPeriod(schedulerPeriod);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Scheduler-Wiederholdauer aktuallisiert: " + schedulerPeriodString);
    }
}
