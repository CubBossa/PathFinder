package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import org.bukkit.Location;

public class FindLocationCommand extends Command {

  public FindLocationCommand() {
    super("findlocation");

    withGeneratedHelp();
    then(CustomArgs.location("location")
        .executesPlayer((player, args) -> {
          FindModule.printResult(FindModule.getInstance().findPath(player, (Location) args[0], 100),
              player);
        })
    );
  }
}
