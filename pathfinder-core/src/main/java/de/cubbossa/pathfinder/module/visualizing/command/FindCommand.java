package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.Bukkit;

public class FindCommand extends Command {

  public FindCommand(PathFinder pathFinder) {
    super(pathFinder, "find");
    withAliases("gps", "navigate");
    withPermission(PathPerms.PERM_CMD_FIND);
    withGeneratedHelp();

    then(CustomArgs.navigateSelectionArgument("selection")
        .executesPlayer((player, args) -> {
          Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
            NodeSelection targets = (NodeSelection) args[0];

              FindModule.getInstance().findPath(player, targets).thenAccept(navigateResult -> {
                  FindModule.printResult(navigateResult, player);
              });
          });
        })
    );
  }
}
