package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class FindCommand extends CommandTree {

  public FindCommand() {
    super("find");
    withAliases("gps", "navigate");
    withPermission(PathPerms.PERM_CMD_FIND);

    then(Arguments.navigateSelectionArgument("selection")
        .executesPlayer((player, args) -> {
          NodeSelectionImpl targets = args.getUnchecked(0);
          if (targets == null) {
            return;
          }

          PathPlayer<Player> p = BukkitPathFinder.wrap(player);

          BukkitNavigationHandler.getInstance().renderPath(p, Route
                  .from()//player loc
                  .toAny(targets))
              .whenComplete((path, throwable) -> {
                if (throwable != null) {
                  player.sendMessage(throwable.getMessage()); // TODO
                  return;
                }
                path.startUpdater(1000);
                BukkitNavigationHandler.getInstance().cancelPathWhenTargetReached(path);
              });

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
