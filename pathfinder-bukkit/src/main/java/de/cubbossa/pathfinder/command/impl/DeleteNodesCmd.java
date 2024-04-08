package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.util.BukkitUtils;
import org.bukkit.command.CommandSender;

public class DeleteNodesCmd extends PathFinderSubCommand {
  public DeleteNodesCmd(PathFinder pathFinder) {
    super(pathFinder, "deletenodes");

    withPermission(PathPerms.PERM_CMD_WP_DELETE);
    then(Arguments.nodeSelectionArgument("nodes")
        .executesPlayer((player, args) -> {
          deleteNode(player, args.getUnchecked(0));
        })
    );
  }

  private void deleteNode(CommandSender sender, NodeSelectionImpl nodes) {
    getPathfinder().getStorage().deleteNodes(nodes.getIds()).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_DELETE.formatted(
          Messages.formatter().nodeSelection("selection", () -> nodes)
      ));
    });
  }
}
