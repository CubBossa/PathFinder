package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.api.misc.Location;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;

public class FindLocationCommand extends Command {

  public FindLocationCommand(PathFinder pathFinder) {
    super(pathFinder, "findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPerms.PERM_CMD_FIND_LOCATION);
    withGeneratedHelp();

    then(CustomArgs.location("location")
        .executesPlayer((player, args) -> {
          Location target = (Location) args[0];

          FindModule.getInstance().findPath(PathPlugin.wrap(player), target).thenAccept(navigateResult -> {
            FindModule.printResult(navigateResult, player);
          });
        })
    );
  }
}
