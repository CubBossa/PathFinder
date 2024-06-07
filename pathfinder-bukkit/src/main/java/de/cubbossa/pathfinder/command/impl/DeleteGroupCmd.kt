package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitUtils;
import org.bukkit.command.CommandSender;

public class DeleteGroupCmd extends PathFinderSubCommand {
  public DeleteGroupCmd(PathFinder pathFinder) {
    super(pathFinder, "deletegroup");

    withGeneratedHelp();
    withPermission(PathPerms.PERM_CMD_NG_DELETE);
    then(Arguments.nodeGroupArgument("group")
        .executes((sender, args) -> {
          deleteGroup(sender, args.getUnchecked(0));
        }));
  }

  private void deleteGroup(CommandSender sender, NodeGroup group) {
    PathPlayer<?> p = BukkitUtils.wrap(sender);
    if (group.getKey().equals(AbstractPathFinder.globalGroupKey())) {
      p.sendMessage(Messages.CMD_NG_DELETE_GLOBAL);
      return;
    }
    getPathfinder().getStorage().deleteGroup(group).thenRun(() -> {
      p.sendMessage(Messages.CMD_NG_DELETE.formatted(
          Messages.formatter().namespacedKey("key", group.getKey())
      ));
    });
  }
}
