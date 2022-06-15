package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.listener.PlayerListener;
import de.bossascrew.pathfinder.util.AStarUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PathPlayer {

    @Getter
    private final UUID uuid;

    /**
     * Key = NodeID
     * Value = FoundInfo Objekt
     */
    private final Map<Integer, FoundInfo> foundFindables;
    private final Map<Integer, FoundInfo> foundGroups;

    private final Map<Integer, ParticlePath> activePaths;
    private final Map<Integer, FindableGroup> lastSetGroups;
    private final Map<Integer, Node> lastSetFindables;

    @Getter
    @Nullable
    private Integer editModeRoadMapId = null;
    @Getter
    @Nullable
    private Integer selectedRoadMapId = null;

    public PathPlayer(int globalPlayerId) {
        this(globalPlayerId, PlayerHandler.getInstance().getGlobalPlayer(globalPlayerId).getPlayerId());
    }

    public PathPlayer(int globalPlayerId, UUID uuid) {
        this.globalPlayerId = globalPlayerId;
        this.uuid = uuid;
        this.activePaths = new HashMap<>();
        this.lastSetGroups = new ConcurrentHashMap<>();
        this.lastSetFindables = new ConcurrentHashMap<>();

        foundFindables = SqlStorage.getInstance().loadFoundNodes(globalPlayerId, false);
        foundGroups = SqlStorage.getInstance().loadFoundNodes(globalPlayerId, true);
    }

    public void find(Node findable, boolean group, Date date) {
        if (group) {
            if (findable.getGroup() != null) {
                find(findable.getGroup().getDatabaseId(), true, date);
            }
        } else {
            find(findable.getNodeId(), false, date);
        }
    }

    public void find(int id, boolean group, Date date) {
        if (hasFound(id, group)) {
            return;
        }
        FoundInfo info = SqlStorage.getInstance().newFoundInfo(globalPlayerId, id, group, date);
        if (group) {
            foundGroups.put(id, info);
        } else {
            foundFindables.put(id, info);
        }
    }

    public void unfind(Node findable, boolean group) {
        unfind(group ? findable.getNodeGroupId() : findable.getNodeId(), group);
    }

    public void unfind(RoadMap roadMap) {
        for (FindableGroup group : roadMap.getGroups().values()) {
            unfind(group.getDatabaseId(), true);
        }
        for (Node findable : roadMap.getFindables()) {
            unfind(findable.getNodeId(), false);
        }
    }

    public void unfind(int id, boolean group) {
        if(group) {
            foundGroups.remove(id);
        } else {
            foundFindables.remove(id);
        }
        SqlStorage.getInstance().deleteFoundNode(globalPlayerId, id, group);
    }

    public boolean hasFound(FindableGroup group) {
        return hasFound(group.getDatabaseId(), true);
    }

    public boolean hasFound(int id, boolean group) {
        if (group) {
            return foundGroups.containsKey(id);
        } else {
            return foundFindables.containsKey(id);
        }
    }

    public boolean hasFound(Node findable) {
        if (findable.getGroup() != null) {
            return foundGroups.containsKey(findable.getGroup().getDatabaseId());
        }
        return foundFindables.containsKey(findable.getNodeId());
    }

    /**
     * Wie viele FoundInfo Objekte der Spieler zu einer Roadmap hat
     */
    public int getFoundAmount(RoadMap roadMap) {
        Collection<Integer> ids = roadMap.getFindables().stream()
                .filter(f -> f.getGroup() == null)
                .filter(this::hasFound)
                .map(Node::getNodeId)
                .collect(Collectors.toSet());
        Collection<Integer> counts = foundGroups.values().stream()
                .map(fi -> roadMap.getFindableGroup(fi.getFoundId()))
                .filter(Objects::nonNull)
                .filter(FindableGroup::isFindable)
                .map(g -> g.getFindables().size())
                .collect(Collectors.toList());

        int count = 0;
        for (int i : counts) {
            count += i;
        }
        return ids.size() + count;
    }

    public void setPath(Node targetNode) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        setPath(player, targetNode);
    }

    public void setPath(final Player player, final Node target) {
        final PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (pathPlayer == null) {
            return;
        }
        final PlayerFindable playerFindable = new PlayerFindable(player, target.getRoadMap());
        PluginUtils.getInstance().runAsync(() -> AStarUtils.startPath(pathPlayer, playerFindable, target, false));
    }

    public void setPath(@NotNull ParticlePath path) {
        ParticlePath active = activePaths.get(path.getRoadMap().getRoadmapId());
        if (active != null) {
            active.cancel();
        }
        path.run(uuid);
        activePaths.put(path.getRoadMap().getRoadmapId(), path);

        Map<Integer, AtomicBoolean> lock = PlayerListener.getHasFoundTarget().getOrDefault(uuid, new ConcurrentHashMap<>());
        lock.put(path.getRoadMap().getRoadmapId(), new AtomicBoolean(false));
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
        ParticlePath toBeCancelled = activePaths.get(roadMap.getRoadmapId());
        if (toBeCancelled == null) {
            return;
        }

        toBeCancelled.cancel();
        activePaths.remove(roadMap.getRoadmapId());
    }

    public void pauseActivePath(RoadMap roadMap) {
        ParticlePath active = activePaths.get(roadMap.getRoadmapId());
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
        ParticlePath paused = activePaths.get(roadMap.getRoadmapId());
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

    public void setEditMode(int roadMapId) {
        this.editModeRoadMapId = roadMapId;
        this.selectedRoadMapId = roadMapId;
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

    public void setSelectedRoadMap(int roadMapId) {
        deselectRoadMap();
        this.selectedRoadMapId = roadMapId;
    }

    public void deselectRoadMap() {
        if(selectedRoadMapId != null) {
            if (editModeRoadMapId != null) {
                RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(editModeRoadMapId);
                if (roadMap != null) {
                    roadMap.setEditMode(this.uuid, false);
                }
            }
        }
        selectedRoadMapId = null;
    }

    public void deselectRoadMap(int id) {
        if (selectedRoadMapId != null && selectedRoadMapId == id) {
            deselectRoadMap();
        }
    }

    public void setLastSetGroup(FindableGroup findableGroup) {
        this.lastSetGroups.put(findableGroup.getRoadMap().getRoadmapId(), findableGroup);
    }

    public @Nullable
    FindableGroup getLastSetGroup(RoadMap roadMap) {
        return this.lastSetGroups.get(roadMap.getRoadmapId());
    }

    public void setLastSetFindable(Node findable) {
        this.lastSetFindables.put(findable.getRoadMapId(), findable);
    }

    public @Nullable
    Node getLastSetFindable(RoadMap roadMap) {
        return this.lastSetFindables.get(roadMap.getRoadmapId());
    }

    public void setVisualizer(RoadMap roadMap, PathVisualizer pathVisualizer) {
        Map<Integer, Integer> map = VisualizerHandler.getInstance().getPlayerVisualizers().getOrDefault(globalPlayerId, new HashMap<>());
        if (map.containsKey(roadMap.getRoadmapId())) {
            SqlStorage.getInstance().updatePlayerVisualizer(globalPlayerId, roadMap, pathVisualizer);
        } else {
            SqlStorage.getInstance().createPlayerVisualizer(globalPlayerId, roadMap, pathVisualizer);
        }
        map.put(roadMap.getRoadmapId(), pathVisualizer.getDatabaseId());
        VisualizerHandler.getInstance().getPlayerVisualizers().put(globalPlayerId, map);

        if (activePaths.containsKey(roadMap.getRoadmapId())) {
            activePaths.get(roadMap.getRoadmapId()).run();
        }
    }

    public PathVisualizer getVisualizer(RoadMap roadMap) {
        Map<Integer, Integer> map = VisualizerHandler.getInstance().getPlayerVisualizers().get(globalPlayerId);
        if (map == null) {
            return roadMap.getPathVisualizer();
        }
        Integer id = map.get(roadMap.getRoadmapId());
        if (id == null) {
            return roadMap.getPathVisualizer();
        }
        return VisualizerHandler.getInstance().getPathVisualizer(id);
    }
}
