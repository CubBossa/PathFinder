package de.bossascrew.pathfinder.core.commands;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.module.visualizing.FindModule;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPICommand;

public class CancelPathCommand extends CommandAPICommand {

    public CancelPathCommand() {
        super("cancelpath");

        withArguments(CustomArgs.roadMapArgument("roadmap"));
        executesPlayer((player, args) -> {
            RoadMap roadMap = (RoadMap) args[0];

            if (roadMap == null) {
                FindModule.getInstance().cancelPaths(player.getUniqueId());

            } else {
                FindModule.getInstance().cancelPath(player.getUniqueId(), roadMap);
            }
            TranslationHandler.getInstance().sendMessage(Messages.CMD_CANCEL, player);
        });
    }
}
