package de.cubbossa.pathfinder.core.commands;

import dev.jorel.commandapi.CommandTree;

public class Command extends CommandTree {

  public Command(String commandName) {
    super(commandName);
  }

  public Command withGeneratedHelp() {
//    executes((sender, args) -> {
//      CommandUtils.sendHelp(sender, this);
//    });
    return this;
  }

  public Command withGeneratedHelp(int depth) {
//    executes((sender, args) -> {
//      CommandUtils.sendHelp(sender, this, depth);
//    });
    return this;
  }
}
