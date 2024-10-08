package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationModule;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class CancelPathCommand extends CommandTree {

  public CancelPathCommand() {
    super("cancelpath");
    NavigationModule<Player> module = NavigationModule.get();

    withPermission(PathPerms.PERM_CMD_CANCELPATH);
    withRequirement(sender -> sender instanceof Player player
        && module.getActiveFindCommandPath(PathPlayer.wrap(player)) != null);

    executesPlayer((player, args) -> {
      var nav = module.getActiveFindCommandPath(PathPlayer.wrap(player));
      if (nav != null) {
        nav.cancel();
      }
    });
  }

  public void refresh(PathPlayer<Player> player) {
    CommandAPI.updateRequirements(player.unwrap());
  }
}
