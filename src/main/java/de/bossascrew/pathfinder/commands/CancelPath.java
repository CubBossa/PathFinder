package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.cubbossa.translations.TranslationHandler;
import org.bukkit.entity.Player;

@CommandAlias("cancelpath")
public class CancelPath extends BaseCommand {

    @Default
    @CommandCompletion(PathPlugin.COMPLETE_ACTIVE_ROADMAPS)
    public void onCancel(Player player, @Optional RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());

        if (roadMap == null) {
            pathPlayer.cancelPaths();

        } else {
            pathPlayer.cancelPath(roadMap);
        }
        TranslationHandler.getInstance().sendMessage(Messages.CMD_CANCEL, player);
    }
}
