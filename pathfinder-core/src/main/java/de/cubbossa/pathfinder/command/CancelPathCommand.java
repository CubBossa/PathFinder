package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.module.FindModule;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.entity.Player;

public class CancelPathCommand extends Command {

  public CancelPathCommand(PathFinder pathFinder) {
    super(pathFinder, "cancelpath");
      withPermission(PathPerms.PERM_CMD_CANCELPATH);
      withRequirement(sender -> sender instanceof Player player
              && FindModule.getInstance().getActivePath(BukkitPathFinder.wrap(player)) != null);

    executesPlayer((player, args) -> {
        FindModule.getInstance().cancelPath(BukkitPathFinder.wrap(player));
    });
  }

  public void refresh(PathPlayer<Player> player) {
    CommandAPI.updateRequirements(player.unwrap());
  }
}
