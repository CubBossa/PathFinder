package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.Optional;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.player.GlobalPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pathfinder.PathPlayer;
import pathfinder.RoadMap;
import pathfinder.handler.PlayerHandler;
import pathfinder.handler.RoadMapHandler;

//TODO commandCompletion alle straßenkarten, die der spieler aktiv an hat.
@CommandAlias("cancelpath")
public class CancelPath extends BaseCommand {

    public void onCancel(Player player, @Optional String roadMapName) {
        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
        assert globalPlayer != null;
        PathPlayer pathPlayer = PlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());

        if(roadMapName == null) {
            pathPlayer.cancelPaths();

        } else {
            RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(roadMapName);
            if(roadMap == null) {
                PlayerUtils.sendMessage(player, ChatColor.RED + "Die angegebene Straßenkarte ist ungültig.");
                return;
            }
            pathPlayer.cancelPath(roadMap);
        }
    }
}
