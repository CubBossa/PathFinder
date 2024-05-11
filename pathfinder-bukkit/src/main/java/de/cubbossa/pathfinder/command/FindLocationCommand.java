package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationLocation;
import de.cubbossa.pathfinder.navigation.NavigationModule;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.CommandTree;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FindLocationCommand extends CommandTree {

  public FindLocationCommand() {
    super("findlocation");
    withAliases("gpslocation", "navigatelocation");
    withPermission(PathPerms.PERM_CMD_FIND_LOCATION);

    then(Arguments.location("location")
        .executesPlayer((player, args) -> {
          NavigationModule<Player> module = NavigationModule.get();
          Location target = args.getUnchecked(0);
          Waypoint waypoint = new Waypoint(UUID.randomUUID());
          waypoint.setLocation(target);

          PathPlayer<Player> p = BukkitUtils.wrap(player);
          module.navigate(p, Route
              .from(NavigationLocation.movingExternalNode(new PlayerNode(p)))
              .to(NavigationLocation.movingExternalNode(waypoint))
          ).whenComplete((path, throwable) -> {
            if (throwable != null) {
              player.sendMessage(throwable.getMessage()); // TODO
              return;
            }
            module.cancelPathWhenTargetReached(path);
          });
        })
    );
  }
}
