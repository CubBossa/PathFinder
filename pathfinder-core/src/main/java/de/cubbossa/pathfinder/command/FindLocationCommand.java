package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.FindModule;
import de.cubbossa.pathfinder.util.BukkitUtils;

public class FindLocationCommand extends Command {

  public FindLocationCommand(PathFinder pathFinder) {
    super(pathFinder, "findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPerms.PERM_CMD_FIND_LOCATION);
    withGeneratedHelp();

    then(CustomArgs.location("location")
        .executesPlayer((player, args) -> {
            Location target = args.getUnchecked(0);

            FindModule.getInstance().findPath(BukkitUtils.wrap(player), target)
              .thenAccept(navigateResult -> {
                FindModule.printResult(navigateResult, player);
              });
        })
    );
  }
}
