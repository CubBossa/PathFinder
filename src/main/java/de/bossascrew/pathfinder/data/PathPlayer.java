package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.visualizer.ParticlePath;
import de.bossascrew.pathfinder.visualizer.SimpleCurveVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.listener.PlayerListener;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapEditor;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
@Setter
public class PathPlayer {

    private final UUID uuid;

    private final Map<Integer, FoundInfo> foundNodes;
    private final Map<Integer, FoundInfo> foundGroups;

    private final Map<NamespacedKey, ParticlePath> activePaths;

    @Nullable
    private NamespacedKey editModeRoadMapId = null;
    @Nullable
    private NamespacedKey selectedRoadMap = null;

    public PathPlayer(Player player) {
        this(player.getUniqueId());
    }

    public PathPlayer(UUID uuid) {
        this.uuid = uuid;
        this.activePaths = new HashMap<>();

        foundNodes = new HashMap<>();
        foundGroups = new HashMap<>();
    }

    public void find(Findable findable, boolean group, Date date) {

    }

    public void forget(Findable findable, boolean deep) {

    }

    public boolean hasFound(Findable findable) {

    }

    /**
     * Wie viele FoundInfo Objekte der Spieler zu einer Roadmap hat
     */
    public int getFoundAmount(RoadMap roadMap) {
        Collection<Integer> ids = roadMap.getNodes().stream()
                .filter(f -> f.getGroup() == null)
                .filter(this::hasFound)
                .map(Waypoint::getNodeId)
                .collect(Collectors.toSet());
        Collection<Integer> counts = foundGroups.values().stream()
                .map(fi -> roadMap.getNodeGroup(fi.getFoundId()))
                .filter(Objects::nonNull)
                .filter(NodeGroup::isFindable)
                .map(g -> g.getFindables().size())
                .collect(Collectors.toList());

        int count = 0;
        for (int i : counts) {
            count += i;
        }
        return ids.size() + count;
    }

    public void setPath(Waypoint targetNode) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        setPath(player, targetNode);
    }

    public void setPath(final Player player, final Waypoint target) {
        final PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (pathPlayer == null) {
            return;
        }
        final PlayerNode playerNode = new PlayerNode(player, target.getRoadMap());
        PluginUtils.getInstance().runAsync(() -> AStarUtils.startPath(pathPlayer, playerNode, target, false));
    }

    public void setPath(@NotNull ParticlePath path) {
        ParticlePath active = activePaths.get(path.getRoadMap().getKey());
        if (active != null) {
            active.cancel();
        }
        path.run(uuid);
        activePaths.put(path.getRoadMap().getKey(), path);

        Map<Integer, AtomicBoolean> lock = PlayerListener.getHasFoundTarget().getOrDefault(uuid, new ConcurrentHashMap<>());
        lock.put(path.getRoadMap().getKey(), new AtomicBoolean(false));
        PlayerListener.getHasFoundTarget().put(uuid, lock);
    }

    public Collection<ParticlePath> getActivePaths() {
        return activePaths.values();
    }

    public void cancelPaths() {
        for (ParticlePath path : activePaths.values()) {
            path.cancel();
        }
        activePaths.clear();
    }

    public void cancelPath(RoadMap roadMap) {
        ParticlePath toBeCancelled = activePaths.get(roadMap.getKey());
        if (toBeCancelled == null) {
            return;
        }

        toBeCancelled.cancel();
        activePaths.remove(roadMap.getKey());
    }

    public void pauseActivePath(RoadMap roadMap) {
        ParticlePath active = activePaths.get(roadMap.getKey());
        if (active == null) {
            return;
        }
        active.cancel();
    }

    public void pauseActivePaths() {
        for (ParticlePath path : activePaths.values()) {
            path.cancel();
        }
    }

    public void resumePausedPath(RoadMap roadMap) {
        ParticlePath paused = activePaths.get(roadMap.getKey());
        if (paused == null) {
            return;
        }
        paused.run(uuid);
    }

    public void resumePausedPaths() {
        for (ParticlePath path : activePaths.values()) {
            path.run(uuid);
        }
    }

    public void setEditMode(NamespacedKey key) {
        this.editModeRoadMapId = key;
    }

    public void clearEditedRoadmap() {
        this.editModeRoadMapId = null;
    }

    public boolean isEditing() {
        return editModeRoadMapId != null;
    }

    public RoadMap getEdited() {
        return RoadMapHandler.getInstance().getRoadMap(editModeRoadMapId);
    }

    public void setSelectedRoadMap(NamespacedKey key) {
        deselectRoadMap();
        this.selectedRoadMap = key;
    }

    public void deselectRoadMap() {
        if (selectedRoadMap != null) {
            if (editModeRoadMapId != null) {
                RoadMapEditor editor = RoadMapHandler.getInstance().getRoadMapEditor(editModeRoadMapId);
                if (editor != null) {
                    editor.setEditMode(this.uuid, false);
                }
            }
        }
        selectedRoadMap = null;
    }

    public void setVisualizer(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {
        Map<Integer, Integer> map = VisualizerHandler.getInstance().getPlayerVisualizers().getOrDefault(globalPlayerId, new HashMap<>());
        if (map.containsKey(roadMap.getKey())) {
            SqlStorage.getInstance().updatePlayerVisualizer(globalPlayerId, roadMap, simpleCurveVisualizer);
        } else {
            SqlStorage.getInstance().createPlayerVisualizer(globalPlayerId, roadMap, simpleCurveVisualizer);
        }
        map.put(roadMap.getKey(), simpleCurveVisualizer.getDatabaseId());
        VisualizerHandler.getInstance().getPlayerVisualizers().put(globalPlayerId, map);

        if (activePaths.containsKey(roadMap.getKey())) {
            activePaths.get(roadMap.getKey()).run();
        }
    }

    public SimpleCurveVisualizer getVisualizer(RoadMap roadMap) {
        Map<Integer, Integer> map = VisualizerHandler.getInstance().getPlayerVisualizers().get(globalPlayerId);
        if (map == null) {
            return roadMap.getSimpleCurveVisualizer();
        }
        Integer id = map.get(roadMap.getKey());
        if (id == null) {
            return roadMap.getSimpleCurveVisualizer();
        }
        return VisualizerHandler.getInstance().getPathVisualizer(id);
    }
}
