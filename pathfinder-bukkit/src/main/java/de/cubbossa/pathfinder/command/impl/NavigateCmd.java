package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationLocation;
import de.cubbossa.pathfinder.navigation.NavigationModule;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.NodeSelection;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class NavigateCmd extends PathFinderSubCommand {

  public NavigateCmd(PathFinder pathFinder) {
    super(pathFinder, "navigate");

    withPermission(PathPerms.PERM_CMD_NAVIGATE);

    then(Arguments.pathPlayers("players")
        .then(Arguments.navigateSelectionArgument("to")
            .executes((sender, args) -> {
              PathPlayer<?> pathSender = PathPlayer.wrap(sender);
              Collection<PathPlayer<Player>> players = args.getUnchecked(0);
              NodeSelection target = args.getUnchecked(1);

              NavigationModule<Player> navigationModule = NavigationModule.get();
              for (PathPlayer<Player> player : players) {
                navigationModule.setFindCommandPath(player, Route
                        .from(NavigationLocation.movingExternalNode(new PlayerNode(player)))
                        .to(target))
                    .whenComplete((nav, throwable) -> {
                      if (throwable != null) {
                        if (throwable instanceof CompletionException) {
                          throwable = throwable.getCause();
                        }
                        if (throwable instanceof NoPathFoundException) {
                          pathSender.sendMessage(Messages.CMD_FIND_BLOCKED);
                        } else if (throwable instanceof GraphEntryNotEstablishedException) {
                          pathSender.sendMessage(Messages.CMD_FIND_TOO_FAR);
                        } else {
                          pathSender.sendMessage(Messages.CMD_FIND_UNKNOWN);
                          PathFinder.get().getLogger().log(Level.SEVERE, "Unknown error while finding path.", throwable);
                        }
                        return;
                      }
                      nav.persist().cancelWhenTargetInRange();
                    });
              }
            })
        )
    );
  }
}
