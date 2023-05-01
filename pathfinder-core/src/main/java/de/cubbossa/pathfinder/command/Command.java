package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import dev.jorel.commandapi.CommandTree;

public class Command extends CommandTree {

  private PathFinder pathFinder;

  public Command(PathFinder pathFinder, String commandName) {
    super(commandName);
    this.pathFinder = pathFinder;
  }

  public Command withGeneratedHelp() {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this);
    });
    return this;
  }

  public Command withGeneratedHelp(int depth) {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this, depth);
    });
    return this;
  }

  public PathFinder getPathfinder() {
    return pathFinder;
  }
}
