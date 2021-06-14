package de.bossascrew.pathfinder.listener;

import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.Node;
import de.bossascrew.pathfinder.PathPlayer;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.RoadMap;
import de.bossascrew.pathfinder.events.NodeGroupFindEvent;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(event.getPlayer().getUniqueId());
        assert globalPlayer != null;
        PathPlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        PathPlayer player = PathPlayerHandler.getInstance().getPlayer(event.getPlayer().getUniqueId());
        assert player != null;
        if (player.isEditing()) {
            player.clearEditMode();
        }

        //TODO speichere gefundendaten eines Spielers in datenbank
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {

        final World world = event.getTo().getWorld();
        final Player player = event.getPlayer();

        PluginUtils.getInstance().runAsync(() -> {
            Collection<RoadMap> roadMaps = RoadMapHandler.getInstance().getRoadMapsFindable(world);
            if (roadMaps.isEmpty()) {
                return;
            }

            if (!player.hasPermission(PathPlugin.PERM_FIND_NODE)) {
                return;
            }
            GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
            assert globalPlayer != null;
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());

            Node found = getFirstNodeInDistance(pathPlayer, event.getTo(), roadMaps);
            if (found == null) {
                return;
            }
            Date findDate = new Date();

            NodeGroupFindEvent findEvent = new NodeGroupFindEvent(globalPlayer.getPlayerId(), found, found.getNodeGroupId(), findDate);
            PluginUtils.getInstance().runSync(() -> {

                Bukkit.getPluginManager().callEvent(findEvent);
                if (event.isCancelled()) {
                    return;
                }
                pathPlayer.findGroup(findEvent.getNode(), findEvent.getDate());

                //TODO Title anzeigen und Sound abspielen (in Methode auslagern)
            });
        });
    }


    private @Nullable
    Node getFirstNodeInDistance(PathPlayer pathPlayer, Location location, Collection<RoadMap> roadMaps) {
        for (RoadMap roadMap : roadMaps) {
            for (Node node : roadMap.getFindableNodes(pathPlayer)) {
                if (node.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                    return node;
                }
            }
        }
        return null;
    }
}
