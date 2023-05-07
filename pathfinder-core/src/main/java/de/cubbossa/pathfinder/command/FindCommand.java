package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.FindModule;
import de.cubbossa.pathfinder.util.NodeSelection;
import org.bukkit.entity.Player;

public class FindCommand extends Command {

  public FindCommand(PathFinder pathFinder) {
    super(pathFinder, "find");
    withAliases("gps", "navigate");
    withPermission(PathPerms.PERM_CMD_FIND);
    withGeneratedHelp();

    then(CustomArgs.navigateSelectionArgument("selection")
        .executesPlayer((player, args) -> {
          NodeSelection targets = args.getUnchecked(0);
          if (targets == null) {
            return;
          }

          PathPlayer<Player> p = BukkitPathFinder.wrap(player);
          FindModule.getInstance().findPath(p, targets).thenAccept(result -> {
            FindModule.printResult(result, p);
          });
        })
    );
  }
}
