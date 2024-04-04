package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.NodeSelection;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.util.NodeUtils;

public class ListNodesCmd extends PathFinderSubCommand {
  public ListNodesCmd(PathFinder pathFinder) {
    super(pathFinder, "listnodes");

    withPermission(PathPerms.PERM_CMD_WP_LIST);
    executesPlayer((sender, args) -> {
      NodeSelection selection = NodeSelection.ofSender("@n", sender);
      NodeUtils.onList(sender, new NodeSelectionImpl(selection), Pagination.page(0, 10));
    });
    then(Arguments.nodeSelectionArgument("nodes")
        .executesPlayer((player, args) -> {
          NodeUtils.onList(player, args.getUnchecked(0), Pagination.page(0, 10));
        })
        .then(Arguments.pagination(10)
            .executesPlayer((player, args) -> {
              NodeUtils.onList(player, args.getUnchecked(0), args.getUnchecked(1));
            })
        ));
  }
}
