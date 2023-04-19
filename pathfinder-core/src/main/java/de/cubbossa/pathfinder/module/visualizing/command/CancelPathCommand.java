package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.entity.Player;

public class CancelPathCommand extends Command {

  public CancelPathCommand(PathFinder pathFinder) {
    super(pathFinder, "cancelpath");
    withPermission(PathPerms.PERM_CMD_CANCELPATH);
    withRequirement(sender -> sender instanceof Player player
        && FindModule.getInstance().getActivePath(PathPlugin.wrap(player)) != null);

    executesPlayer((player, args) -> {
      FindModule.getInstance().cancelPath(PathPlugin.wrap(player));
    });
  }

  public void refresh(PathPlayer<Player> player) {
    CommandAPI.updateRequirements(player.unwrap());
  }
}
