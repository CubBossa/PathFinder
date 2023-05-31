package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.util.NodeUtils;

public class ListNodesCmd extends PathFinderSubCommand {
  public ListNodesCmd(PathFinder pathFinder) {
    super(pathFinder, "listnodes");

    withPermission(PathPerms.PERM_CMD_WP_LIST);
    then(CustomArgs.nodeSelectionArgument("nodes")
        .executesPlayer((player, args) -> {
          NodeUtils.onList(player, args.getUnchecked(0), Pagination.page(0, 10));
        })
        .then(CustomArgs.pagination(10)
            .executesPlayer((player, args) -> {
              NodeUtils.onList(player, args.getUnchecked(0), args.getUnchecked(1));
            })
        ));
  }
}
