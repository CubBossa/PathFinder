package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;

public class CreateNodeCmd extends PathFinderSubCommand {

  public CreateNodeCmd(PathFinder pathFinder, Supplier<NodeType<?>> fallbackWaypointType) {
    super(pathFinder, "createnode");

    withPermission(PathPerms.PERM_CMD_WP_CREATE);
    executesPlayer((player, args) -> {
      createNode(player, fallbackWaypointType.get(),
          BukkitVectorUtils.toInternal(player.getLocation()));
    });
    then(Arguments.location("location")
        .displayAsOptional()
        .executesPlayer((player, args) -> {
          createNode(player, fallbackWaypointType.get(), args.getUnchecked(0));
        })
    );
    then(Arguments.nodeTypeArgument("type")
        .executesPlayer((player, args) -> {
          createNode(player, args.getUnchecked(0),
              BukkitVectorUtils.toInternal(player.getLocation()));
        })
        .then(Arguments.location("location")
            .executesPlayer((player, args) -> {
              createNode(player, args.getUnchecked(0),
                  args.getUnchecked(1));
            })
        )
    );
  }


  private void createNode(CommandSender sender, NodeType<?> type, Location location) {
    getPathfinder().getStorage().createAndLoadNode(type, location).thenAccept(n -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_CREATE.insert("node", n));
    });
  }
}
