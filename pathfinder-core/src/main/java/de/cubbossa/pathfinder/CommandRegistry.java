package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.commands.NodeGroupCommand;
import de.cubbossa.pathfinder.core.commands.PathFinderCommand;
import de.cubbossa.pathfinder.core.commands.WaypointCommand;
import de.cubbossa.pathfinder.module.visualizing.command.PathVisualizerCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import dev.jorel.commandapi.CommandTree;
import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

  private final PathFinder pathFinder;

  private PathFinderCommand pathFinderCommand;
  private NodeGroupCommand nodeGroupCommand;
  private PathVisualizerCommand pathVisualizerCommand;
  private WaypointCommand waypointCommand;

  private final List<CommandTree> externalCommands;

  public CommandRegistry(PathFinder pathFinder) {
    this.pathFinder = pathFinder;
    this.externalCommands = new ArrayList<>();
  }

  public void registerCommand(CommandTree command) {
    externalCommands.add(command);
  }

  public void unregisterCommand(CommandTree command) {
    externalCommands.remove(command);
  }

  public void loadCommands() {
    CommandAPI.onLoad(new CommandAPIConfig());
  }

  public void enableCommands(PathPlugin plugin) {
    CommandAPI.onEnable(plugin);
    pathFinderCommand = new PathFinderCommand(pathFinder);
    pathFinderCommand.register();
    nodeGroupCommand = new NodeGroupCommand(pathFinder, 0);
    nodeGroupCommand.register();
    pathVisualizerCommand = new PathVisualizerCommand(pathFinder);
    pathVisualizerCommand.register();
    waypointCommand = new WaypointCommand(pathFinder, plugin::getWaypointNodeType);
    waypointCommand.register();
    externalCommands.forEach(CommandTree::register);
  }

  public void unregisterCommands() {
    unregister(pathFinderCommand);
    unregister(nodeGroupCommand);
    unregister(pathVisualizerCommand);
    unregister(waypointCommand);
    externalCommands.forEach(this::unregister);
    CommandAPI.onDisable();
  }

  private void unregister(CommandTree tree) {
    if (tree != null) {
      CommandAPI.unregister(tree.getName());
    }
  }
}
