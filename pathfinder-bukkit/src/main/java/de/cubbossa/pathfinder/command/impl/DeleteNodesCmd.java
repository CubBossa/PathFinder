package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

public class DeleteNodesCmd extends PathFinderSubCommand {
  public DeleteNodesCmd(PathFinder pathFinder) {
    super(pathFinder, "deletenodes");

    withPermission(PathPerms.PERM_CMD_WP_DELETE);
    then(CustomArgs.nodeSelectionArgument("nodes")
        .executesPlayer((player, args) -> {
          deleteNode(player, args.getUnchecked(0));
        })
    );
  }

  private void deleteNode(CommandSender sender, NodeSelection nodes) {
    getPathfinder().getStorage().deleteNodes(nodes.ids()).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_DELETE.formatted(
          Placeholder.component("selection", Messages.formatNodeSelection(sender, nodes))
      ));
    });
  }
}
