package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.NodeUtils;
import de.cubbossa.pathfinder.util.SelectionUtils;

public class ListNodesCmd extends PathFinderSubCommand {
  public ListNodesCmd(PathFinder pathFinder) {
    super(pathFinder, "listnodes");

    withPermission(PathPerms.PERM_CMD_WP_LIST);
    executesPlayer((sender, args) -> {
      NodeSelection selection = SelectionUtils.getNodeSelection(sender, "@n");
      NodeUtils.onList(sender, new NodeSelection(selection), Pagination.page(0, 10));
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
