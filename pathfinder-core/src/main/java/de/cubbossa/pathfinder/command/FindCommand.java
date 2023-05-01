package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.FindModule;
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
            Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), () -> {
                NodeSelection targets = args.getUnchecked(0);

                FindModule.getInstance().findPath(BukkitPathFinder.wrap(player), targets)
                        .thenAccept(navigateResult -> {
                            FindModule.printResult(navigateResult, player);
                        });
            });
        })
    );
  }
}
