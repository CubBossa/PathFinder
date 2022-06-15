package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("cancelpath")
public class CancelPath extends BaseCommand {

    @Default
    @CommandCompletion(PathPlugin.COMPLETE_ACTIVE_ROADMAPS)
    public void onCancel(Player player, @Optional String roadMapName) {
        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
        if (globalPlayer == null) {
            return;
        }
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());

        if (roadMapName == null) {
            pathPlayer.cancelPaths();

        } else {
            RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(roadMapName);
            if (roadMap == null) {
                PlayerUtils.sendMessage(player, ChatColor.RED + "Die angegebene Straßenkarte ist ungültig.");
                return;
            }
            pathPlayer.cancelPath(roadMap);
        }
        PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Wegweisung abgebrochen.");
    }
}
