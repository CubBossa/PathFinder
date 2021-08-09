package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;


//TODO checks dass werte nicht negativ sind und so weiter

@CommandAlias("editmode-visualizer|emv")
public class EditModeVisualizerCommand extends BaseCommand {

    @Subcommand("list")
    @CommandPermission("bcrew.command.visualizer.editmode.list")
    public void onList(CommandSender sender) {

        ComponentMenu menu = new ComponentMenu(Component.text("Editmode-Visualizer").color(NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, true));

        for (EditModeVisualizer vis : VisualizerHandler.getInstance().getEditModeVisualizers()) {
            menu.addSub(new ComponentMenu(Component.text(vis.getName() + "(#" + vis.getDatabaseId() + ")", PathPlugin.COLOR_DARK)
					.append(Component.text(", Parent: ", NamedTextColor.GRAY))
					.append(vis.getParent() == null ?
							CommandUtils.NULL_COMPONENT :
							Component.text(vis.getParent().getName(), PathPlugin.COLOR_LIGHT))
					.clickEvent(ClickEvent.runCommand("/emv info " + vis.getName()))));
        }
        PlayerUtils.sendComponents(sender, menu.toComponents());
    }


    @Subcommand("create")
    @Syntax("<Name> <Partikel>")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_PARTICLES_LOWERCASE + " " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    @CommandPermission("bcrew.command.visualizer.editmode.create")
    public void onCreate(CommandSender sender, @Single String name, @Optional Particle particle, @Optional String parent) {

        if (!VisualizerHandler.getInstance().isNameUniqueEditMode(name)) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben");
            return;
        }
        EditModeVisualizer parentVis = VisualizerHandler.getInstance().getEditModeVisualizer(parent);
        if (parentVis == null) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Unbekannter Visualizer: \"" + parent + "\". Setze Default-Visualizer");
        }
        VisualizerHandler.getInstance().createEditModeVisualizer(name, parentVis, particle, null, null, null, null, null);

		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Editmode-Visualizer erstellt: " + PathPlugin.CHAT_COLOR_LIGHT + name);
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Nutze /emv " + name + " <Einstellung> <Wert>, um ihn zu bearbeiten");
    }

    @Subcommand("delete")
    @CommandPermission("bcrew.command.visualizer.editmode.delete")
    @Syntax("<Editmode-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onDelete(CommandSender sender, EditModeVisualizer visualizer) {
        if (!VisualizerHandler.getInstance().deleteEditModeVisualizer(visualizer)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Fehler beim Löschen von Editmode-Visualizer: " + visualizer.getName() + ".");
            return;
        }
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Visualizer erfolgreich gelöscht.");
    }

    @Subcommand("info")
    @CommandPermission("bcrew.command.visualizer.editmode.info")
    @Syntax("<Editmode-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onInfo(CommandSender sender, EditModeVisualizer visualizer) {

        ComponentMenu menu = new ComponentMenu(Component.text("Editmode-Visualizer: ", NamedTextColor.WHITE)
                .append(Component.text(visualizer.getName() + " (#" + visualizer.getDatabaseId() + ")")
						.color(PathPlugin.COLOR_DARK)
                        .hoverEvent(HoverEvent.showText(Component.text("Klicken zum Umbenennnen")))
                        .clickEvent(ClickEvent.suggestCommand("/emv set name " + visualizer.getName() + " <Neuer Name>"))));

        menu.addSub(new ComponentMenu(Component.text("Parent: ")
                .append(CommandUtils.getParentList(visualizer))
                .hoverEvent(HoverEvent.showText(Component.text("Parent setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set parent " + visualizer.getName() + " <Parent>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticle() == null ? null : Component.text(visualizer1.getUnsafeParticle().name(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle " + visualizer.getName() + " <Partikel>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Limit: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleLimit() == null ? null : Component.text(visualizer1.getUnsafeParticleLimit(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Limit setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle-limit " + visualizer.getName() + " <Partikellimit>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Distanz: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeParticleDistance() == null ? null : Component.text(visualizer1.getUnsafeParticleDistance(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle-distance " + visualizer.getName() + " <Partikeldistanz>"))));

        menu.addSub(new ComponentMenu(Component.text("Scheduler-Wiederholrate: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeSchedulerPeriod() == null ? null : Component.text(visualizer1.getUnsafeSchedulerPeriod(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Scheduler-Wiederholrate setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set scheduler-period " + visualizer.getName() + " <Wiederholrate in Ticks>"))));

        menu.addSub(new ComponentMenu(Component.text("Node Head-ID: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeNodeHeadId() == null ? null : Component.text(visualizer1.getUnsafeNodeHeadId(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Node Head-ID setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set node-head-id " + visualizer.getName() + " <HeadID>"))));

        menu.addSub(new ComponentMenu(Component.text("Edge Head-ID: ")
                .append(CommandUtils.getPropertyComponent(visualizer, visualizer1 ->
						visualizer1.getUnsafeEdgeHeadId() == null ? null : Component.text(visualizer1.getUnsafeEdgeHeadId(), PathPlugin.COLOR_LIGHT)))
                .hoverEvent(HoverEvent.showText(Component.text("Edge Head-ID setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set edge-head-id " + visualizer.getName() + " <HeadID>"))));

        PlayerUtils.sendComponents(sender, menu.toComponents());
    }

    @Subcommand("set parent")
    @CommandPermission("bcrew.command.visualizer.editmode.set.parent")
    @Syntax("<Editmode-Visualizer> <Parent>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetParent(CommandSender sender, EditModeVisualizer edit, EditModeVisualizer parent) {
        if (edit.getDatabaseId() == parent.getDatabaseId() || parent.hasParent(edit)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + parent.getName() + " ist ein ungültiger oder bereits gesetzter Parent.");
            return;
        }
        edit.setParent(parent);
		DatabaseModel.getInstance().updateEditModeVisualizer(edit);
		PlayerUtils.sendMessage(sender, PathPlugin.CHAT_COLOR_LIGHT + "Parent aktualisiert: " + parent.getName());
    }

    @Subcommand("set name")
    @CommandPermission("bcrew.command.visualizer.editmode.set.name")
    @Syntax("<Editmode-Visualizer> <Neuer Name>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetName(CommandSender sender, EditModeVisualizer edit, @Single String newName) {
        edit.setAndSaveName(newName);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Name aktualisiert: " + newName);
    }

    @Subcommand("set particle")
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle")
    @Syntax("<Editmode-Visualizer> <Partikel>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " " + BukkitMain.COMPLETE_PARTICLES_LOWERCASE)
    public void onSetParticle(CommandSender sender, EditModeVisualizer edit, String particleName) {
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
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle-limit")
    @Syntax("<Editmode-Visualizer> <Limit>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " null")
    public void onSetParticleLimit(CommandSender sender, EditModeVisualizer edit, String limitString) {
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
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle-distance")
    @Syntax("<Editmode-Visualizer> <Distanz>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " null")
    public void onSetParticleDistance(CommandSender sender, EditModeVisualizer edit, String distanceString) {
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

    @Subcommand("set scheduler-period")
    @CommandPermission("bcrew.command.visualizer.editmode.set.scheduler-period")
    @Syntax("<Editmode-Visualizer> <Scheduler-Wiederholabstand>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " null")
    public void onSetSchedulerPeriod(CommandSender sender, EditModeVisualizer edit, String schedulerPeriodString) {
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

    @Subcommand("set node-head-id")
    @CommandPermission("bcrew.command.visualizer.editmode.set.node-head-id")
    @Syntax("<Editmode-Visualizer> <Kopf-ID>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " null")
    public void onSetNodeHeadId(CommandSender sender, EditModeVisualizer edit, String idString) {
        Integer nodeHeadId = null;
        if (!idString.equalsIgnoreCase("null")) {
            try {
                nodeHeadId = Integer.parseInt(idString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + idString);
                return;
            }
        }
        edit.setAndSaveNodeHeadId(nodeHeadId);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Head-ID aktualisiert: " + idString);
    }

    @Subcommand("set edge-head-id")
    @CommandPermission("bcrew.command.visualizer.editmode.set.edge-head-id")
    @Syntax("<Editmode-Visualizer> <Kopf-ID>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " null")
    public void onSetEdgeHeadId(CommandSender sender, EditModeVisualizer edit, String idString) {
        Integer edgeHeadId = null;
        if (!idString.equalsIgnoreCase("null")) {
            try {
                edgeHeadId = Integer.parseInt(idString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + idString);
                return;
            }
        }
        edit.setAndSaveEdgeHeadId(edgeHeadId);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Edge-Head-ID aktualisiert: " + idString);
    }
}


