package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class CancelPathCommand extends CommandTree {

	public CancelPathCommand() {
		super("cancelpath");
		withPermission(PathPlugin.PERM_CMD_CANCELPATH);
		withRequirement(sender -> sender instanceof Player player && FindModule.getInstance().getActivePath(player) != null);

		executesPlayer((player, args) -> {
			FindModule.getInstance().cancelPath(player.getUniqueId());
		});
	}

	public void refresh(Player player) {
		CommandAPI.updateRequirements(player);
	}
}
