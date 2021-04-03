package de.bossascrew.pathfinder.listener;

import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.pathfinder.Node;
import de.bossascrew.pathfinder.PathPlayer;
import de.bossascrew.pathfinder.RoadMap;
import de.bossascrew.pathfinder.handler.PlayerHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import de.bossascrew.pathfinder.handler.RoadMapHandler;

import java.util.Collection;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(event.getPlayer().getUniqueId());
        assert globalPlayer != null;
        PlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        PathPlayer player = PlayerHandler.getInstance().getPlayer(event.getPlayer().getUniqueId());
        assert player != null;
        if(player.isEditing()) player.clearEditMode();

        //speichere gefundendaten eines Spielers in datenbank
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        //todo permission check für finden

        World world = event.getTo().getWorld();
        Collection<RoadMap> roadMaps = RoadMapHandler.getInstance().getRoadMapsFindable(world);
        assert !roadMaps.isEmpty();

        //TODO asynchron? damit server nicht unter move event leidet
        checkFoundNode(event.getPlayer(), event.getTo(), roadMaps);
    }

    private void checkFoundNode(Player player, Location location, Collection<RoadMap> roadMaps) {
        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
        assert globalPlayer != null;
        PathPlayer pathPlayer = PlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());

        for(RoadMap roadMap : roadMaps) {
            for(Node node : roadMap.getNodes()) { //TODO sich die finable nodes holen
                if(pathPlayer.hasFound(node.getDatabaseId())) {
                    if(node.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                        pathPlayer.findGroup(node);
                    }
                }
            }
        }
    }
}
