package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.Bukkit;

public class FindCommand extends CommandTree {

	public FindCommand() {
		super("find");
		withAliases("gps", "navigate");
		withPermission(PathPlugin.PERM_CMD_FIND);

		then(new LiteralArgument("location")
				.withPermission(PathPlugin.PERM_CMD_FIND_LOC)
				.then(CustomArgs.navigateSelectionArgument("selection")
						.executesPlayer((player, args) -> {
							Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
								NodeSelection targets = (NodeSelection) args[0];
								switch (FindModule.getInstance().findPath(player, targets)) {
									case SUCCESS -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND, player);
									case FAIL_BLOCKED -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_BLOCKED, player);
									case FAIL_EMPTY -> TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND_EMPTY, player);
								}
							});
						})
				)
		);
	}
}
