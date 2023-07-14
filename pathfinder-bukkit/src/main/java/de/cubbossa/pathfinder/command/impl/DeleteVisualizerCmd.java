package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class DeleteVisualizerCmd extends PathFinderSubCommand {
  public DeleteVisualizerCmd(PathFinder pathFinder) {
    super(pathFinder, "deletevisualizer");

    withGeneratedHelp();
    withPermission(PathPerms.PERM_CMD_PV_DELETE);
    then(Arguments.pathVisualizerArgument("visualizer")
        .executes((commandSender, objects) -> {
          onDelete(commandSender, objects.getUnchecked(0));
        })
    );
  }

  public void onDelete(CommandSender sender, PathVisualizer<?, ?> visualizer) {
    getPathfinder().getStorage().deleteVisualizer(visualizer).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_DELETE_SUCCESS.formatted(
          Messages.formatter().namespacedKey("key", visualizer.getKey())
      ));
    }).exceptionally(throwable -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_DELETE_ERROR);
      getPathfinder().getLogger().log(Level.WARNING, "Could not delete visualizer", throwable);
      return null;
    });
  }
}
