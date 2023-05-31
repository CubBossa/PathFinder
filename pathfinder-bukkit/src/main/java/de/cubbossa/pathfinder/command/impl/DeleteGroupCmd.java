package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

public class DeleteGroupCmd extends PathFinderSubCommand {
  public DeleteGroupCmd(PathFinder pathFinder) {
    super(pathFinder, "deletegroup");

    withGeneratedHelp();
    withPermission(PathPerms.PERM_CMD_NG_DELETE);
    then(CustomArgs.nodeGroupArgument("group")
        .executes((sender, args) -> {
          deleteGroup(sender, args.getUnchecked(0));
        }));
  }

  private void deleteGroup(CommandSender sender, NodeGroup group) {
    PathPlayer<?> p = BukkitUtils.wrap(sender);
    if (group.getKey().equals(CommonPathFinder.globalGroupKey())) {
      p.sendMessage(Messages.CMD_NG_DELETE_GLOBAL);
      return;
    }
    getPathfinder().getStorage().deleteGroup(group).thenRun(() -> {
      p.sendMessage(Messages.CMD_NG_DELETE.formatted(
          Placeholder.parsed("name", group.getKey().toString())
      ));
    });
  }
}
