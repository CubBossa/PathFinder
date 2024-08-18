package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.tinytranslations.util.ListSection;
import org.bukkit.command.CommandSender;

public class ListGroupsCmd extends PathFinderSubCommand {
  public ListGroupsCmd(PathFinder pathFinder) {
    super(pathFinder, "listgroups");

    withPermission(PathPerms.PERM_CMD_NG_LIST);
    executes((sender, args) -> {
      listGroups(sender, Pagination.page(0, 10));
    });
    then(Arguments.pagination(10)
        .executes((sender, args) -> {
          listGroups(sender, args.getUnchecked(0));
        })
    );
  }

  private void listGroups(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadGroups(pagination).thenAccept(nodeGroups -> {
      PathPlayer.wrap(sender).sendMessage(Messages.CMD_NG_LIST
          .insertList("groups", nodeGroups, ListSection.paged(pagination.getPage(), pagination.getSize()))
      );
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }
}
