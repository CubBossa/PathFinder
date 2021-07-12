package de.bossascrew.pathfinder.listener;

import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.ParticlePath;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.events.NodeGroupFindEvent;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import java.util.stream.Collectors;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(event.getPlayer().getUniqueId());
        if (globalPlayer == null) {
            return;
        }
        PathPlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        PathPlayer player = PathPlayerHandler.getInstance().getPlayer(event.getPlayer().getUniqueId());
        if (player == null) {
            return;
        }
        if (player.isEditing()) {
            player.getEdited().setEditMode(player.getUuid(), false);
        }
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {

        final World world = event.getTo().getWorld();
        final Player player = event.getPlayer();

        PluginUtils.getInstance().runAsync(() -> {
             PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
            for (ParticlePath path : pPlayer.getActivePaths()) {
                RoadMap rm = path.getRoadMap();
                Findable findable = path.get(path.size() - 1);
                if (event.getTo().toVector().distance(findable.getVector()) < rm.getNodeFindDistance()) {
                    pPlayer.cancelPath(rm);
                    player.sendMessage(Component.text("Ziel erreicht: ", NamedTextColor.GRAY)
                            .append(Component.text(findable.getName(), NamedTextColor.WHITE)));
                }
            }
        });
        PluginUtils.getInstance().runAsync(() -> {
            GlobalPlayer globalPlayer = de.bossascrew.core.player.PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
            if (globalPlayer == null) {
                return;
            }
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(globalPlayer.getDatabaseId());

            Collection<RoadMap> roadMaps = RoadMapHandler.getInstance().getRoadMapsFindable(world);
            if (roadMaps.isEmpty()) {
                return;
            }
            if (pathPlayer.isEditing()) {
                return;
            }
            if (!player.hasPermission(PathPlugin.PERM_FIND_NODE)) {
                return;
            }
            Findable found = getFirstNodeInDistance(player, pathPlayer, event.getTo(), roadMaps);
            if (found == null) {
                return;
            }
            Date findDate = new Date();

            NodeGroupFindEvent findEvent = new NodeGroupFindEvent(globalPlayer.getPlayerId(), found, found.getNodeGroupId() == null ? null : found.getNodeGroupId(), findDate);
            PluginUtils.getInstance().runSync(() -> {

                Bukkit.getPluginManager().callEvent(findEvent);
                if (event.isCancelled()) {
                    return;
                }
                pathPlayer.findGroup(findEvent.getNode(), findEvent.getDate());

                RoadMap rm = found.getRoadMap();
                double percent = 100 * ((double) pathPlayer.getFoundAmount(found.getRoadMap())) / rm.getFindables().stream().filter(f -> f.getGroup() == null || f.getGroup().isFindable()).count();

                player.showTitle(Title.title(Component.empty(), Component.text("Entdeckt: ").color(NamedTextColor.GRAY)
                        .append(Component.text(found.getGroup() != null ? found.getGroup().getName() : found.getName()).color(NamedTextColor.WHITE))));
                player.playSound(found.getVector().toLocation(rm.getWorld()), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 1);
                player.sendActionBar(
                        Component.text(rm.getName() + " erkundet: ", NamedTextColor.GRAY)
                        .append(Component.text(String.format("%,.2f", percent) + "%", NamedTextColor.WHITE)));
            });
        });
    }

    private @Nullable
    Findable getFirstNodeInDistance(Player player, PathPlayer pathPlayer, Location location, Collection<RoadMap> roadMaps) {
        for (RoadMap roadMap : roadMaps) {
            for (FindableGroup group : roadMap.getGroups().values()) {
                if (!group.isFindable()) {
                    continue;
                }
                for (Findable findable : group.getFindables()) {
                    if (pathPlayer.hasFound(findable.getDatabaseId())) {
                        continue;
                    }
                    if (findable.getPermission() != null && !player.hasPermission(findable.getPermission())) {
                        continue;
                    }
                    if (findable.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                        return findable;
                    }
                }
            }
            for (Findable findable : roadMap.getFindables().stream().filter(rm -> rm.getGroup() == null).collect(Collectors.toSet())) {
                if (pathPlayer.hasFound(findable.getDatabaseId())) {
                    continue;
                }
                if (findable.getPermission() != null && !player.hasPermission(findable.getPermission())) {
                    continue;
                }
                if (findable.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                    return findable;
                }
            }
        }
        return null;
    }
}
