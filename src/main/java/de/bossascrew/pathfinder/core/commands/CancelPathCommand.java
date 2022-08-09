package de.bossascrew.pathfinder.core.commands;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPICommand;

public class CancelPathCommand extends CommandAPICommand {

    public CancelPathCommand() {
        super("cancelpath");

        withArguments(CustomArgs.roadMapArgument("roadmap"));
        executesPlayer((player, args) -> {
            RoadMap roadMap = (RoadMap) args[0];
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());

            if (roadMap == null) {
                pathPlayer.cancelPaths();

            } else {
                pathPlayer.cancelPath(roadMap);
            }
            TranslationHandler.getInstance().sendMessage(Messages.CMD_CANCEL, player);
        });
    }
}
