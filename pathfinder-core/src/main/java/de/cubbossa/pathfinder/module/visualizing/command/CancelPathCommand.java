package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.entity.Player;

public class CancelPathCommand extends Command {

  public CancelPathCommand() {
    super("cancelpath");
    withPermission(PathPerms.PERM_CMD_CANCELPATH);
    withRequirement(sender -> sender instanceof Player player
        && FindModule.getInstance().getActivePath(player) != null);

    executesPlayer((player, args) -> {
      FindModule.getInstance().cancelPath(player.getUniqueId());
    });
  }

  public void refresh(Player player) {
    CommandAPI.updateRequirements(player);
  }
}
