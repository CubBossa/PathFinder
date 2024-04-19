package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.navigation.NavigationLocation;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class FindCommand extends CommandTree {

  public FindCommand() {
    super("find");
    withAliases("gps", "navigate");
    withPermission(PathPerms.PERM_CMD_FIND);

    then(Arguments.navigateSelectionArgument("selection")
        .executesPlayer((player, args) -> {
          NodeSelectionImpl targets = args.getUnchecked(0);
          if (targets == null) {
            return;
          }
          PathPlayer<Player> p = PathPlayer.wrap(player);
          if (targets.isEmpty()) {
            p.sendMessage(Messages.CMD_FIND_EMPTY);
            return;
          }
          BukkitNavigationHandler.getInstance().navigate(p, Route
                  .from(NavigationLocation.movingExternalNode(new PlayerNode(PathPlayer.wrap(player))))
                  .toAny(targets))
              .whenComplete((path, throwable) -> {
                if (throwable != null) {
                  if (throwable instanceof NoPathFoundException) {
                    p.sendMessage(Messages.CMD_FIND_BLOCKED);
                  } else if (throwable instanceof GraphEntryNotEstablishedException) {
                    p.sendMessage(Messages.CMD_FIND_TOO_FAR);
                  } else {
                    p.sendMessage(Messages.CMD_FIND_UNKNOWN);
                  }
                  return;
                }
                path.startUpdater(1000);
                BukkitNavigationHandler.getInstance().cancelPathWhenTargetReached(path);
                p.sendMessage(Messages.CMD_FIND);
              });
        })
    );
  }
}
