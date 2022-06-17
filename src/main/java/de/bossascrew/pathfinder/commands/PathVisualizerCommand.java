package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;

/**
 * Command zum erstellen, löschen und bearbeiten von Pfadvisualizer-Profilen
 */
@CommandAlias("path-visualizer|pathv|pv")
public class PathVisualizerCommand extends BaseCommand {

    @Subcommand("list")
    @CommandPermission("pathfinder.command.visualizer.path.list")
    public void onList(CommandSender sender) {

        ComponentMenu menu = new ComponentMenu(Component.text("Pfad-Visualizer").color(NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, true));

        for (SimpleCurveVisualizer vis : VisualizerHandler.getInstance().getPathVisualizers()) {
            menu.addSub(new ComponentMenu(Component.text(vis.getNameFormat() + "(#" + vis.getDatabaseId() + ")", PathPlugin.COLOR_DARK)
					.append(Component.text(", Parent: ", NamedTextColor.GRAY))
					.append(vis.getParent() == null ?
							CommandUtils.NULL_COMPONENT :
							Component.text(vis.getParent().getName(), PathPlugin.COLOR_LIGHT))
					.clickEvent(ClickEvent.runCommand("/path-visualizer info " + vis.getNameFormat()))));
        }
        PlayerUtils.sendComponents(sender, menu.toComponents());
    }


    @Subcommand("create")
    @Syntax("<Name> <Partikel>")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_PARTICLES_LOWERCASE + " " + PathPlugin.COMPLETE_PATH_VISUALIZER)
    @CommandPermission("pathfinder.command.visualizer.path.create")
    public void onCreate(CommandSender sender, @Single String name, @Optional Particle particle, @Optional String parent) {

        if (!VisualizerHandler.getInstance().isNameUniquePath(name)) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben");
            return;
        }
        SimpleCurveVisualizer parentVis = VisualizerHandler.getInstance().getPathVisualizer(parent);
        if (parentVis == null) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Unbekannter Visualizer: \"" + parent + "\". Setze Default-Visualizer");
        }
        VisualizerHandler.getInstance().createPathVisualizer(name, parentVis, particle, null, null, null, null);

		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Pfad-Visualizer erstellt: " + PathPlugin.CHAT_COLOR_LIGHT + name);
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Nutze /path-visualizer " + name + " <Einstellung> <Wert>, um ihn zu bearbeiten");
    }

    @Subcommand("delete")
    @CommandPermission("pathfinder.command.visualizer.path.delete")
    @Syntax("<Pfad-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onDelete(CommandSender sender, SimpleCurveVisualizer visualizer) {
        if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Fehler beim Löschen von Pfad-Visualizer: " + visualizer.getNameFormat() + ".");
            return;
        }
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Visualizer erfolgreich gelöscht.");
    }

    @Subcommand("info")
    @CommandPermission("pathfinder.command.visualizer.path.info")
    @Syntax("<Pfad-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onInfo(CommandSender sender, SimpleCurveVisualizer visualizer) {

        ComponentMenu menu = new ComponentMenu(Component.text("Pfad-Visualizer: ", NamedTextColor.WHITE)
                .append(Component.text(visualizer.getNameFormat() + " (#" + visualizer.getDatabaseId() + ")")
						.color(PathPlugin.COLOR_DARK)
                        .hoverEvent(HoverEvent.showText(Component.text("Klicken zum Umbenennnen")))
                        .clickEvent(ClickEvent.suggestCommand("/path-visualizer set name " + visualizer.getNameFormat() + " <Neuer Name>"))));

        menu.addSub(new ComponentMenu(Component.text("Parent: ")
                .append(CommandUtils.getParentList(visualizer))
                .hoverEvent(HoverEvent.showText(Component.text("Parent setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set parent " + visualizer.getNameFormat() + " <Parent>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticle() == null ? null : Component.text(visualizer1.getUnsafeParticle().name(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle " + visualizer.getNameFormat() + " <Partikel>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Limit: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleLimit() == null ? null : Component.text(visualizer1.getUnsafeParticleLimit(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Limit setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-limit " + visualizer.getNameFormat() + " <Partikellimit>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Distanz: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleDistance() == null ? null : Component.text(visualizer1.getUnsafeParticleDistance(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-distance " + visualizer.getNameFormat() + " <Partikeldistanz>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Schritte: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleSteps() == null ? null : Component.text(visualizer1.getUnsafeParticleSteps(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set particle-steps " + visualizer.getNameFormat() + " <Partikelschritte>"))));

        menu.addSub(new ComponentMenu(Component.text("Scheduler-Wiederholrate: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeSchedulerPeriod() == null ? null : Component.text(visualizer1.getUnsafeSchedulerPeriod(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Scheduler-Wiederholrate setzen")))
                .clickEvent(ClickEvent.suggestCommand("/path-visualizer set scheduler-period " + visualizer.getNameFormat() + " <Wiederholrate in Ticks>"))));

        PlayerUtils.sendComponents(sender, menu.toComponents());
    }

    @Subcommand("set parent")
    @CommandPermission("pathfinder.command.visualizer.path.set.parent")
    @Syntax("<Pfad-Visualizer> <Parent>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " " + PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onSetParent(CommandSender sender, SimpleCurveVisualizer edit, SimpleCurveVisualizer parent) {
        if (edit.getDatabaseId() == parent.getDatabaseId() || parent.hasParent(edit)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + parent.getNameFormat() + " ist ein ungültiger oder bereits gesetzter Parent.");
            return;
        }
        edit.setParent(parent);
		SqlStorage.getInstance().updatePathVisualizer(edit);
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Parent aktualisiert: " + parent.getNameFormat());
    }

    @Subcommand("set name")
    @CommandPermission("pathfinder.command.visualizer.path.set.name")
    @Syntax("<Pfad-Visualizer> <Neuer Name>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
    public void onSetName(CommandSender sender, SimpleCurveVisualizer edit, String newName) {
        newName = StringUtils.replaceSpaces(newName);
        edit.setAndSaveName(newName);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Name aktualisiert: " + newName);
    }

    @Subcommand("set particle")
    @CommandPermission("pathfinder.command.visualizer.path.set.particle")
    @Syntax("<Pfad-Visualizer> <Partikel>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " " + BukkitMain.COMPLETE_PARTICLES_LOWERCASE)
    public void onSetParticle(CommandSender sender, SimpleCurveVisualizer edit, String particleName) {
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikeleffekt aktualisiert: " + (particle != null ? particle.name() : "null"));
    }

    @Subcommand("set particle-limit")
    @CommandPermission("pathfinder.command.visualizer.path.set.particle-limit")
    @Syntax("<Pfad-Visualizer> <Limit>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER  + " null")
    public void onSetParticleLimit(CommandSender sender, SimpleCurveVisualizer edit, @Single String limitString) {
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikellimit aktualisiert: " + limitString);
    }

    @Subcommand("set particle-distance")
    @CommandPermission("pathfinder.command.visualizer.path.set.particle-distance")
    @Syntax("<Pfad-Visualizer> <Distanz>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER  + " null")
    public void onSetParticleDistance(CommandSender sender, SimpleCurveVisualizer edit, @Single String distanceString) {
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikeldistanz aktualisiert: " + distanceString);
    }

    @Subcommand("set particle-steps")
    @CommandPermission("pathfinder.command.visualizer.path.set.particle-steps")
    @Syntax("<Pfad-Visualizer> <Schritte>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
    public void onSetParticleSteps(CommandSender sender, SimpleCurveVisualizer edit, @Single String stepString) {
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikelschritte aktualisiert: " + stepString);
    }

    @Subcommand("set scheduler-period")
    @CommandPermission("pathfinder.command.visualizer.path.set.scheduler-period")
    @Syntax("<Pfad-Visualizer> <Scheduler-Wiederholabstand>")
    @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
    public void onSetSchedulerPeriod(CommandSender sender, SimpleCurveVisualizer edit, @Single String schedulerPeriodString) {
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Scheduler-Wiederholdauer aktualisiert: " + schedulerPeriodString);
    }

    @Subcommand("style")
    public class Style extends BaseCommand {

        @Subcommand("name")
        @Syntax("<Pfad-Visualizer> <Name>")
        @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
        public void onName(CommandSender sender, SimpleCurveVisualizer visualizer, String name) {
            if (!visualizer.isPickable()) {
                visualizer.createPickable(null, name, null);
            } else {
                visualizer.setDisplayName(name);
                SqlStorage.getInstance().updateVisualizerStyle(visualizer);
            }
            sender.sendMessage(Component.empty()
                    .append(PathPlugin.PREFIX_COMP)
                    .append(Component.text("Name gesetzt: ", NamedTextColor.GRAY))
                    .append(ComponentUtils.parseMiniMessage(name)));
        }

        @Subcommand("material")
        @Syntax("<Pfad-Visualizer> <Material>")
        @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " " + BukkitMain.COMPLETE_MATERIALS_LOWERCASE)
        public void onMaterial(CommandSender sender, SimpleCurveVisualizer visualizer, Material type) {
            if (!visualizer.isPickable()) {
                visualizer.createPickable(null, null, type);
            } else {
                visualizer.setIconType(type);
                SqlStorage.getInstance().updateVisualizerStyle(visualizer);
            }
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Typ gesetzt: " + PathPlugin.COLOR_LIGHT + type);
        }

        @Subcommand("permission")
        @Syntax("<Pfad-Visualizer> <Permission>")
        @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER + " null")
        public void onPermission(CommandSender sender, SimpleCurveVisualizer visualizer, @Single String permission) {
            if (!visualizer.isPickable()) {
                visualizer.createPickable(permission, null, null);
            } else {
                visualizer.setPermission(permission);
                SqlStorage.getInstance().updateVisualizerStyle(visualizer);
            }
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Permission gesetzt: " + PathPlugin.COLOR_LIGHT + permission);
        }

        @Subcommand("delete")
        @Syntax("<Pfad-Visualizer>")
        @CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
        public void onDelete(CommandSender sender, SimpleCurveVisualizer visualizer) {
            if(!visualizer.isPickable()) {
                PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Der Visualizer muss ein Style sein.");
                return;
            }
            visualizer.removePickable();
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style erfolgreich gelöscht: " + visualizer.getNameFormat());
        }
    }
}
