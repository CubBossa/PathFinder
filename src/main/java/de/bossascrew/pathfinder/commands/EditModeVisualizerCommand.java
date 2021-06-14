package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.visualisation.EditModeVisualizer;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;

@CommandAlias("editmode-visualizer|emv")
public class EditModeVisualizerCommand extends BaseCommand {

    @Subcommand("create")
    @Syntax("<Name> <Partikel>")
    @CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER + " " + BukkitMain.COMPLETE_NOTHING + " " +
            PathPlugin.COMPLETE_PARTICLES)
    @CommandPermission("bcrew.command.visualizer.editmode.create")
    public void onCreate(CommandSender sender, @Single String name, Particle particle) {
        EditModeVisualizer edit = VisualizerHandler.getInstance().getEditVisualizer("default");
        assert edit != null;

        if (!VisualizerHandler.getInstance().isNameUniqueEditMode(name)) {
            PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Der Name ist bereits vergeben");
        }
        DatabaseModel.getInstance().newEditModeVisualizer(name, particle,
                edit.getParticleDistance(),
                edit.getParticleLimit(),
                edit.getSchedulerStartDelay(),
                edit.getSchedulerPeriod(),
                edit.getNodeHeadId(),
                edit.getEdgeHeadId());
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Editmode-Visualizer erstellt: " + ChatColor.GREEN + name);
    }
}
