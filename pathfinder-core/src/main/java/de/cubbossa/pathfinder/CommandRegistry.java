package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.command.NodeGroupCommand;
import de.cubbossa.pathfinder.command.PathFinderCommand;
import de.cubbossa.pathfinder.command.PathVisualizerCommand;
import de.cubbossa.pathfinder.command.WaypointCommand;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

  private final PathFinder pathFinder;
  private final List<CommandTree> externalCommands;
  private PathFinderCommand pathFinderCommand;
  private NodeGroupCommand nodeGroupCommand;
  private PathVisualizerCommand pathVisualizerCommand;
  private WaypointCommand waypointCommand;

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
    CommandAPI.onLoad(new CommandAPIBukkitConfig(PathFinderPlugin.getInstance())
        .shouldHookPaperReload(true)
        .missingExecutorImplementationMessage("Wrong command usage, use /help."));
  }

  public void enableCommands(CommonPathFinder plugin) {
    CommandAPI.onEnable();
    pathFinderCommand = new PathFinderCommand(pathFinder);
    pathFinderCommand.register();
    nodeGroupCommand = new NodeGroupCommand(pathFinder);
    nodeGroupCommand.register();
    pathVisualizerCommand = new PathVisualizerCommand(pathFinder);
    pathVisualizerCommand.register();
    NodeType<?> type = plugin.getNodeTypeRegistry().getType(CommonPathFinder.pathfinder("waypoint"));
    waypointCommand = new WaypointCommand(pathFinder, () -> type);
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
