package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
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

  private void deleteNode(CommandSender sender, NodeSelection nodes) {
    getPathfinder().getStorage().deleteNodes(nodes.ids()).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_DELETE.formatted(
          Messages.formatter().nodeSelection("selection", () -> nodes)
      ));
    });
  }
}
