package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;

public class FindCommand extends CommandTree {

	public FindCommand() {
		super("find");
		withAliases("gps", "navigate");
		withPermission(PathPlugin.PERM_CMD_FIND);

		then(new LiteralArgument("location")
				.withPermission(PathPlugin.PERM_CMD_FIND_LOC)
				.then(CustomArgs.navigateSelectionArgument("selection")
						.executesPlayer((player, args) -> {
							NodeSelection targets = (NodeSelection) args[0];
							if (targets.size() == 0) {
								//TODO message
								return;
							}
							FindModule.getInstance().findPath(player, targets);
							TranslationHandler.getInstance().sendMessage(Messages.CMD_FIND, player);
						})
				)
		);
	}
}
