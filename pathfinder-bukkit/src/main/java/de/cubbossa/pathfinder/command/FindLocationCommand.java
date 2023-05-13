package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.util.BukkitUtils;
import org.bukkit.entity.Player;

public class FindLocationCommand extends Command {

  public FindLocationCommand(PathFinder pathFinder) {
    super(pathFinder, "findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPerms.PERM_CMD_FIND_LOCATION);
    withGeneratedHelp();

    then(CustomArgs.location("location")
        .executesPlayer((player, args) -> {
          Location target = args.getUnchecked(0);

          PathPlayer<Player> p = BukkitUtils.wrap(player);
          BukkitNavigationHandler.getInstance().findPath(p, target).thenAccept(result -> {
            AbstractNavigationHandler.printResult(result, p);
          });
        })
    );
  }
}
