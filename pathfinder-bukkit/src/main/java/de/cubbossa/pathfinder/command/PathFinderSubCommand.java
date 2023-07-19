package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import dev.jorel.commandapi.arguments.LiteralArgument;

public class PathFinderSubCommand extends LiteralArgument {

  private PathFinder pathFinder;

  public PathFinderSubCommand(PathFinder pathFinder, String commandName) {
    super(commandName);
    this.pathFinder = pathFinder;
  }

  public PathFinderSubCommand withGeneratedHelp() {
    executes((sender, args) -> {
      //CommandUtils.sendHelp(sender, this);
    });
    return this;
  }

  public PathFinderSubCommand withGeneratedHelp(int depth) {
    executes((sender, args) -> {
      //CommandUtils.sendHelp(sender, this, depth);
    });
    return this;
  }

  public PathFinder getPathfinder() {
    return pathFinder;
  }
}
