package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class FindLocationCommand extends CommandTree {

  public FindLocationCommand() {
    super("findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPerms.PERM_CMD_FIND_LOCATION);

    then(Arguments.location("location")
        .executesPlayer((player, args) -> {
          Location target = args.getUnchecked(0);

          PathPlayer<Player> p = BukkitUtils.wrap(player);
          BukkitNavigationHandler.getInstance().findPathToLocation(p, target).thenAccept(result -> {
            AbstractNavigationHandler.printResult(result, p);
          }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
          });
        })
    );
  }
}
