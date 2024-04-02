package de.cubbossa.pathfinder;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathfinder.command.FindPlayerManager;
import de.cubbossa.pathfinder.command.PathFinderCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry implements Disposable {

  private final PathFinder pathFinder;
  private final List<CommandTree> externalCommands;
  private PathFinderCommand pathFinderCommand;

  public CommandRegistry(PathFinder pathFinder) {
    this.pathFinder = pathFinder;
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.externalCommands = new ArrayList<>();
  }

  @Override
  public void dispose() {
    unregisterCommands();
  }

  public void registerCommand(CommandTree command) {
    externalCommands.add(command);
  }

  public void unregisterCommand(CommandTree command) {
    externalCommands.remove(command);
  }

  public void loadCommands() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(PathFinderPlugin.getInstance())
        .shouldHookPaperReload(true)
        .missingExecutorImplementationMessage("Wrong command usage, use /help."));
  }

  public void enableCommands(AbstractPathFinder plugin) {
    CommandAPI.onEnable();
    pathFinderCommand = new PathFinderCommand(pathFinder);
    pathFinderCommand.register();
    new FindPlayerManager(plugin, this);
    externalCommands.forEach(CommandTree::register);
  }

  public void unregisterCommands() {
    unregister(pathFinderCommand);
    externalCommands.forEach(this::unregister);
    CommandAPI.onDisable();
  }

  private void unregister(CommandTree tree) {
    if (tree != null) {
      CommandAPI.unregister(tree.getName());
    }
  }
}
