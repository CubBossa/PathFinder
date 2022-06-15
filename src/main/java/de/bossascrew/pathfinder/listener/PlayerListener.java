package de.bossascrew.pathfinder.listener;

import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.core.player.PlayerHandler;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.ParticlePath;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.events.NodeFindEvent;
import de.bossascrew.pathfinder.events.NodeGroupFindEvent;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import lombok.Getter;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Getter
    private static final Map<UUID, Map<Integer, AtomicBoolean>> hasFoundTarget = new ConcurrentHashMap<>();

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {

        final World world = event.getTo().getWorld();
        final Player player = event.getPlayer();

        PluginUtils.getInstance().runAsync(() -> {
            PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
            if (pPlayer != null) {
                findPathTarget(event, player, pPlayer);
            }

            discoverFindables(event, world, player);
        });
    }

    private void findPathTarget(PlayerMoveEvent event, Player player, PathPlayer pPlayer) {
        for (ParticlePath path : pPlayer.getActivePaths()) {
            if(!player.getWorld().equals(path.getRoadMap().getWorld())) {
                continue;
            }
            RoadMap rm = path.getRoadMap();
            Node findable = path.get(path.size() - 1);
            AtomicBoolean foundGuard = hasFoundTarget.getOrDefault(player.getUniqueId(), new HashMap<>())
                    .getOrDefault(rm.getRoadmapId(), new AtomicBoolean(true));
            if (event.getTo().toVector().distance(findable.getVector()) < rm.getNodeFindDistance() && !foundGuard.getAndSet(true)) {
                pPlayer.cancelPath(rm);
                player.sendMessage(PathPlugin.PREFIX_COMP.append(Component.text("Ziel erreicht: ", NamedTextColor.GRAY))
                        .append(Component.text(findable.getGroup() != null ? findable.getGroup().getFriendlyName() : findable.getFriendlyName(), NamedTextColor.WHITE)));

                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1, 1);
            }
        }
    }

    private void discoverFindables(PlayerMoveEvent event, World world, Player player) {
        GlobalPlayer globalPlayer = PlayerHandler.getInstance().getGlobalPlayer(player.getUniqueId());
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
        Node found = getFirstNodeInDistance(player, pathPlayer, event.getTo(), roadMaps);
        if (found == null) {
            return;
        }

        PluginUtils.getInstance().runSync(() -> {
            Date findDate = new Date();
            boolean group = found.getGroup() != null;

            int id;
            if (group) {
                NodeGroupFindEvent findEvent = new NodeGroupFindEvent(globalPlayer.getPlayerId(), found.getGroup(), found, findDate);
                Bukkit.getPluginManager().callEvent(findEvent);
                findDate = findEvent.getDate();
                id = findEvent.getGroup().getDatabaseId();
            } else {
                NodeFindEvent findEvent = new NodeFindEvent(globalPlayer.getPlayerId(), found, findDate);
                Bukkit.getPluginManager().callEvent(findEvent);
                findDate = findEvent.getDate();
                id = findEvent.getFindable().getNodeId();
            }
            if (event.isCancelled()) {
                return;
            }
            if ((group && pathPlayer.hasFound(found.getGroup())) || pathPlayer.hasFound(found)) {
                return;
            }
            pathPlayer.find(id, group, findDate);

            RoadMap rm = found.getRoadMap();
            double percent = 100 * ((double) pathPlayer.getFoundAmount(found.getRoadMap())) / rm.getMaxFoundSize();

            player.showTitle(Title.title(Component.empty(), Component.text("Entdeckt: ").color(NamedTextColor.GRAY)
                    .append(Component.text(found.getGroup() != null ? found.getGroup().getFriendlyName() : found.getFriendlyName()).color(NamedTextColor.WHITE))));
            player.playSound(found.getVector().toLocation(rm.getWorld()), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 1);
            player.sendActionBar(
                    Component.text(rm.getNameFormat() + " erkundet: ", NamedTextColor.GRAY)
                            .append(Component.text(String.format("%,.2f", percent) + "%", NamedTextColor.WHITE)));
        });
    }

    private @Nullable
    Node getFirstNodeInDistance(Player player, PathPlayer pathPlayer, Location location, Collection<RoadMap> roadMaps) {
        for (RoadMap roadMap : roadMaps) {
            if(roadMap.isEdited()) {
                continue;
            }
            for (FindableGroup group : roadMap.getGroups().values()) {
                if (!group.isFindable()) {
                    continue;
                }
                if(pathPlayer.hasFound(group.getDatabaseId(), true)) {
                    continue;
                }
                for (Node findable : group.getFindables()) {
                    if (findable.getPermission() != null && !player.hasPermission(findable.getPermission())) {
                        continue;
                    }
                    if (findable.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                        return findable;
                    }
                }
            }
            for (Node findable : roadMap.getFindables().stream().filter(f -> f.getGroup() == null).collect(Collectors.toSet())) {
                if (pathPlayer.hasFound(findable.getNodeId(), false)) {
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
