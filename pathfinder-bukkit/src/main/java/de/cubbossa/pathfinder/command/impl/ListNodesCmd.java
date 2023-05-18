package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
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
          NodeUtils.onList(player, args.getUnchecked(0), 1);
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executesPlayer((player, args) -> {
              NodeUtils.onList(player, args.getUnchecked(0), args.getUnchecked(1));
            })
        ));
  }


}
