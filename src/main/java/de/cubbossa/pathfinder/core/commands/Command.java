package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.util.CommandUtils;
import dev.jorel.commandapi.CommandTree;

public class Command extends CommandTree {

  public Command(String commandName) {
    super(commandName);

    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this);
    });
  }
}
