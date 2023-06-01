package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ListGroupsCmd extends PathFinderSubCommand {
  public ListGroupsCmd(PathFinder pathFinder) {
    super(pathFinder, "listgroups");

    withPermission(PathPerms.PERM_CMD_NG_LIST);
    executes((sender, args) -> {
      listGroups(sender, Pagination.page(0, 10));
    });
    then(CustomArgs.pagination(10)
        .executes((sender, args) -> {
          listGroups(sender, args.getUnchecked(0));
        })
    );
  }

  private void listGroups(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadGroups(pagination).thenAccept(nodeGroups -> {
      CommandUtils.printList(
          sender,
          pagination,
          new ArrayList<>(nodeGroups),
          group -> {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_LIST_LINE.formatted(
                Messages.formatter().namespacedKey("key", group.getKey()),
                Messages.formatter().number("size", group.size()),
                Messages.formatter().number("weight", group.getWeight()),
                Messages.formatter().modifiers("modifiers", group.getModifiers())
            ));
          },
          Messages.CMD_NG_LIST_HEADER,
          Messages.CMD_NG_LIST_FOOTER
      );
    });
  }
}
