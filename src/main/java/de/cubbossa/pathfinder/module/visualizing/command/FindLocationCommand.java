package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import org.bukkit.Location;

public class FindLocationCommand extends Command {

  public FindLocationCommand() {
    super("findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPlugin.PERM_CMD_FIND_LOCATION);
    withGeneratedHelp();

    then(CustomArgs.location("location")
        .executesPlayer((player, args) -> {
          Location target = (Location) args[0];
          FindModule.printResult(FindModule.getInstance().findPath(player, target), player);
        })
    );
  }
}
