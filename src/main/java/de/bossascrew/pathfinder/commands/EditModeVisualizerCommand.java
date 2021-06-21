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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;

@CommandAlias("editmode-visualizer|emv")
public class EditModeVisualizerCommand extends BaseCommand {

    private interface VisualizerProperty<T> {
        Component accept(T visualizer);
    }

    public static final Component NULL_COMPONENT = Component.text("null", NamedTextColor.GRAY);

    @Subcommand("list")
    @CommandPermission("bcrew.command.visualizer.editmode.list")
    public void onList(CommandSender sender) {

        ComponentMenu menu = new ComponentMenu(Component.text("Editmode-Visualizer").color(NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, true));

        for (EditModeVisualizer vis : VisualizerHandler.getInstance().getEditModeVisualizers()) {
            menu.addSub(new ComponentMenu(Component.text(vis.getName() + "(#" + vis.getDatabaseId() + ")", NamedTextColor.DARK_GREEN)
                    .append(Component.text(", Parent: ", NamedTextColor.GRAY))
                    .append(vis.getParent() == null ?
                            NULL_COMPONENT :
                            Component.text(vis.getParent().getName(), NamedTextColor.GREEN))
                    .clickEvent(ClickEvent.runCommand("/emv info " + vis.getName()))));
        }
        PlayerUtils.sendComponents(sender, menu.toComponents());
    }


    @Subcommand("create")
    @Syntax("<Name> <Partikel>")
    @CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + PathPlugin.COMPLETE_PARTICLES + " " + PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    @CommandPermission("bcrew.command.visualizer.editmode.create")
    public void onCreate(CommandSender sender, @Single String name, @Optional Particle particle, @Optional String parent) {

        if (!VisualizerHandler.getInstance().isNameUniqueEditMode(name)) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben");
            return;
        }
        EditModeVisualizer parentVis = VisualizerHandler.getInstance().getEditVisualizer(parent);
        if (parentVis == null) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Unbekannter Visualizer: \"" + parent + "\". Setze Default-Visualizer");
        }
        VisualizerHandler.getInstance().createEditModeVisualizer(name, parentVis, particle, null, null, null, null, null);

        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Editmode-Visualizer erstellt: " + ChatColor.GREEN + name);
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
        PlayerUtils.sendMessage(sender, ChatColor.GREEN + "Visualizer erfolgreich gelöscht.");
    }

    @Subcommand("info")
    @CommandPermission("bcrew.command.visualizer.editmode.info")
    @Syntax("<Editmode-Visualizer>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onInfo(CommandSender sender, EditModeVisualizer visualizer) {

        ComponentMenu menu = new ComponentMenu(Component.text("Editmode-Visualizer: ", NamedTextColor.WHITE)
                .append(Component.text(visualizer.getName() + " (#" + visualizer.getDatabaseId() + ")")
                        .color(NamedTextColor.DARK_GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Klicken zum Umbenennnen")))
                        .clickEvent(ClickEvent.suggestCommand("/emv set name " + visualizer.getName() + " <Neuer Name>"))));

        menu.addSub(new ComponentMenu(Component.text("Parent: ")
                .append(getParentComponent(visualizer, false, visualizer1 -> Component.text(visualizer1.getName(), NamedTextColor.GREEN)))
                .hoverEvent(HoverEvent.showText(Component.text("Parent setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set parent " + visualizer.getName() + " <Parent>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel: ")
                .append(getParentComponent(visualizer, true, visualizer1 ->
                        visualizer1.getUnsafeParticle() == null ? null : Component.text(visualizer1.getUnsafeParticle().name(), NamedTextColor.GREEN)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle " + visualizer.getName() + " <Partikel>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Limit: ")
                .append(getParentComponent(visualizer, true, visualizer1 ->
                        visualizer1.getUnsafeParticleLimit() == null ? null : Component.text(visualizer1.getUnsafeParticleLimit(), NamedTextColor.GREEN)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Limit setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle-limit " + visualizer.getName() + " <Partikellimit>"))));

        menu.addSub(new ComponentMenu(Component.text("Partikel-Distanz: ")
                .append(getParentComponent(visualizer, true, visualizer1 ->
                        visualizer1.getUnsafeParticleDistance() == null ? null : Component.text(visualizer1.getUnsafeParticleDistance(), NamedTextColor.GREEN)))
                .hoverEvent(HoverEvent.showText(Component.text("Partikel-Distanz setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set particle-distance " + visualizer.getName() + " <Partikeldistanz>"))));

        menu.addSub(new ComponentMenu(Component.text("Scheduler-Wiederholrate: ")
                .append(getParentComponent(visualizer, true, visualizer1 ->
                        visualizer1.getUnsafeSchedulerPeriod() == null ? null : Component.text(visualizer1.getUnsafeSchedulerPeriod(), NamedTextColor.GREEN)))
                .hoverEvent(HoverEvent.showText(Component.text("Scheduler-Wiederholrate setzen")))
                .clickEvent(ClickEvent.suggestCommand("/emv set scheduler-period " + visualizer.getName() + " <Wiederholrate in Ticks>"))));

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
        PlayerUtils.sendMessage(sender, ChatColor.GREEN + "Parent aktuallisiert: " + parent.getName());
    }

    @Subcommand("set name")
    @CommandPermission("bcrew.command.visualizer.editmode.set.name")
    @Syntax("<Editmode-Visualizer> <Neuer Name>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetName(CommandSender sender, EditModeVisualizer edit, @Single String newName) {
        edit.setAndSaveName(newName);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Name aktuallisiert: " + newName);
    }

    @Subcommand("set particle")
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle")
    @Syntax("<Editmode-Visualizer> <Partikel>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " " + PathPlugin.COMPLETE_PARTICLES)
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
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikeleffekt aktuallisiert: " + (particle != null ? particle.name() : "null"));
    }

    @Subcommand("set particle-limit")
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle-limit")
    @Syntax("<Editmode-Visualizer> <Limit>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetParticleLimit(CommandSender sender, EditModeVisualizer edit, String limitString) {
        Integer limit = null;
        if (limitString.equalsIgnoreCase("null")) {
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
    @CommandPermission("bcrew.command.visualizer.editmode.set.particle-distance")
    @Syntax("<Editmode-Visualizer> <Distanz>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetParticleDistance(CommandSender sender, EditModeVisualizer edit, String distanceString) {
        Double distance = null;
        if (distanceString.equalsIgnoreCase("null")) {
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

    @Subcommand("set scheduler-period")
    @CommandPermission("bcrew.command.visualizer.editmode.set.scheduler-period")
    @Syntax("<Editmode-Visualizer> <Scheduler-Wiederholabstand>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetSchedulerPeriod(CommandSender sender, EditModeVisualizer edit, String schedulerPeriodString) {
        Integer schedulerPeriod = null;
        if (schedulerPeriodString.equalsIgnoreCase("null")) {
            try {
                schedulerPeriod = Integer.parseInt(schedulerPeriodString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + schedulerPeriodString);
                return;
            }
        }
        edit.setAndSaveNodeHeadId(schedulerPeriod);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Scheduler-Wiederholdauer aktuallisiert: " + schedulerPeriodString);
    }

    @Subcommand("set node-head-id")
    @CommandPermission("bcrew.command.visualizer.editmode.set.node-head-id")
    @Syntax("<Editmode-Visualizer> <Kopf-ID>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetNodeHeadId(CommandSender sender, EditModeVisualizer edit, String idString) {
        Integer nodeHeadId = null;
        if (idString.equalsIgnoreCase("null")) {
            try {
                nodeHeadId = Integer.parseInt(idString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + idString);
                return;
            }
        }
        edit.setAndSaveNodeHeadId(nodeHeadId);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Head-ID aktuallisiert: " + idString);
    }

    @Subcommand("set edge-head-id")
    @CommandPermission("bcrew.command.visualizer.editmode.set.edge-head-id")
    @Syntax("<Editmode-Visualizer> <Kopf-ID>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
    public void onSetEdgeHeadId(CommandSender sender, EditModeVisualizer edit, String idString) {
        Integer edgeHeadId = null;
        if (idString.equalsIgnoreCase("null")) {
            try {
                edgeHeadId = Integer.parseInt(idString);
            } catch (IllegalArgumentException exc) {
                PlayerUtils.sendMessage(sender, ChatColor.RED + "Ungültiger Integer-Wert: " + idString);
                return;
            }
        }
        edit.setAndSaveEdgeHeadId(edgeHeadId);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Edge-Head-ID aktuallisiert: " + idString);
    }

    private Component getParentComponent(EditModeVisualizer visualizer, boolean cancelAtFirstValid, VisualizerProperty<EditModeVisualizer> property) {
        return getParentComponent(Component.empty(), visualizer, property, true, cancelAtFirstValid);
    }

    private Component getParentComponent(Component component, EditModeVisualizer visualizer, VisualizerProperty<EditModeVisualizer> property, boolean first, boolean cancelAtFirstValid) {
        Component separator = Component.text(first ? "" : "«", NamedTextColor.DARK_GRAY);
        if (visualizer == null) {
            return component;
        }
        Component propertyComp = property.accept(visualizer);
        Component part = Component.empty().append(separator).append(propertyComp == null ? NULL_COMPONENT : propertyComp);
        if (visualizer.getParent() != null && (propertyComp == null || !cancelAtFirstValid)) {
            return component.append(part).append(getParentComponent(component, visualizer.getParent(), property, false, cancelAtFirstValid));
        } else {
            return component.append(part);
        }
    }
}


