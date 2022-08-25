package de.cubbossa.pathfinder.core.listener;

import de.cubbossa.pathfinder.data.PathPlayer;
import de.cubbossa.pathfinder.data.PathPlayerHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PathPlayerHandler.getInstance().getPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        PathPlayer player = PathPlayerHandler.getInstance().getPlayer(event.getPlayer().getUniqueId());
        if (player == null) {
            return;
        }
        if (player.isEditing()) {
            RoadMapHandler.getInstance().getRoadMapEditor(player.getEdited().getKey()).setEditMode(player.getUuid(), false);
        }
    }

    @Getter
    private static final Map<UUID, Map<Integer, AtomicBoolean>> hasFoundTarget = new ConcurrentHashMap<>();
/*
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
            Waypoint findable = path.get(path.size() - 1);
            AtomicBoolean foundGuard = hasFoundTarget.getOrDefault(player.getUniqueId(), new HashMap<>())
                    .getOrDefault(rm.getKey(), new AtomicBoolean(true));
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
        Waypoint discoverable = getFirstNodeInDistance(player, pathPlayer, event.getTo(), roadMaps);
        if (discoverable == null) {
            return;
        }

        PluginUtils.getInstance().runSync(() -> {
            Date findDate = new Date();
            boolean group = discoverable.getGroup() != null;

            int id;
            if (group) {
                NodeGroupFindEvent findEvent = new NodeGroupFindEvent(globalPlayer.getPlayerId(), discoverable.getGroup(), discoverable, findDate);
                Bukkit.getPluginManager().callEvent(findEvent);
                findDate = findEvent.getDate();
                id = findEvent.getGroup().getGroupId();
            } else {
                NodeFindEvent findEvent = new NodeFindEvent(globalPlayer.getPlayerId(), discoverable, findDate);
                Bukkit.getPluginManager().callEvent(findEvent);
                findDate = findEvent.getDate();
                id = findEvent.getFindable().getNodeId();
            }
            if (event.isCancelled()) {
                return;
            }
            if ((group && pathPlayer.hasFound(discoverable.getGroup())) || pathPlayer.hasFound(discoverable)) {
                return;
            }
            pathPlayer.find(id, group, findDate);

            RoadMap rm = discoverable.getRoadMap();
            double percent = 100 * ((double) pathPlayer.getFoundAmount(discoverable.getRoadMap())) / rm.getMaxFoundSize();

            player.showTitle(Title.title(Component.empty(), Component.text("Entdeckt: ").color(NamedTextColor.GRAY)
                    .append(Component.text(discoverable.getGroup() != null ? discoverable.getGroup().getFriendlyName() : discoverable.getFriendlyName()).color(NamedTextColor.WHITE))));
            player.playSound(discoverable.getVector().toLocation(rm.getWorld()), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1, 1);
            player.sendActionBar(
                    Component.text(rm.getNameFormat() + " erkundet: ", NamedTextColor.GRAY)
                            .append(Component.text(String.format("%,.2f", percent) + "%", NamedTextColor.WHITE)));
        });
    }

    private @Nullable
	Waypoint getFirstNodeInDistance(Player player, PathPlayer pathPlayer, Location location, Collection<RoadMap> roadMaps) {
        for (RoadMap roadMap : roadMaps) {
            if(roadMap.isEdited()) {
                continue;
            }
            for (NodeGroup group : roadMap.getGroups().values()) {
                if (!group.isFindable()) {
                    continue;
                }
                if(pathPlayer.hasFound(group.getGroupId(), true)) {
                    continue;
                }
                for (Waypoint findable : group.getFindables()) {
                    if (findable.getPermission() != null && !player.hasPermission(findable.getPermission())) {
                        continue;
                    }
                    if (findable.getVector().distance(location.toVector()) < roadMap.getNodeFindDistance()) {
                        return findable;
                    }
                }
            }
            for (Waypoint findable : roadMap.getNodes().stream().filter(f -> f.getGroup() == null).collect(Collectors.toSet())) {
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
    }*/
}
