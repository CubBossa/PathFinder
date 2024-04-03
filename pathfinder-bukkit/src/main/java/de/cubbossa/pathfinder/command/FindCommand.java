package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.util.NodeSelection;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class FindCommand extends CommandTree {

  public FindCommand() {
    super("find");
    withAliases("gps", "navigate");
    withPermission(PathPerms.PERM_CMD_FIND);

    then(Arguments.navigateSelectionArgument("selection")
        .executesPlayer((player, args) -> {
          NodeSelection targets = args.getUnchecked(0);
          if (targets == null) {
            return;
          }

          PathPlayer<Player> p = BukkitPathFinder.wrap(player);
          BukkitNavigationHandler.getInstance().findPathToNodes(p, targets).thenAccept(result -> {
            AbstractNavigationHandler.printResult(result, p);
          }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
          });
        })
    );
  }
}
