package de.bossascrew.pathfinder.data;

import com.google.common.collect.Maps;
import de.bossascrew.core.player.PlayerHandler;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.util.AStarUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PathPlayer {

    @Getter
    private final int globalPlayerId;
    @Getter
    private final UUID uuid;

    /**
     * Key = NodeID
     * Value = FoundInfo Objekt
     */
    private final Map<Integer, FoundInfo> foundInfos;

    private final Map<Integer, ParticlePath> activePaths;

    @Getter
    @Nullable
    private Integer editModeRoadMapId = null;
    @Getter
    @Nullable
    private Integer selectedRoadMapId = null;

    public PathPlayer(int globalPlayerId, UUID uuid) {
        this.globalPlayerId = globalPlayerId;
        this.uuid = uuid;
        this.activePaths = new HashMap<>();

        foundInfos = Maps.newHashMap();
    }

    public PathPlayer(int globalPlayerId) {
        this.globalPlayerId = globalPlayerId;
        this.uuid = PlayerHandler.getInstance().getGlobalPlayer(globalPlayerId).getPlayerId();
        this.activePaths = new HashMap<>();

        foundInfos = DatabaseModel.getInstance().loadFoundNodes(globalPlayerId);
    }

    public void find(Findable findable, boolean group, Date date) {
        if (group) {
            findGroup(findable, date);
        } else {
            find(findable, date);
        }
    }

    public void findGroup(Findable findable, Date date) {
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(findable.getRoadMapId());
        if (roadMap == null) {
            return;
        }
        FindableGroup group = roadMap.getFindableGroup(findable.getNodeGroupId());

        if (group == null) {
            find(findable, date);
        } else {
            for (Findable grouped : group.getFindables()) {
                find(grouped, date);
            }
        }
    }

    public void find(Findable findable, Date date) {
        if (hasFound(findable.getDatabaseId())) {
            return;
        }
        FoundInfo info = DatabaseModel.getInstance().newFoundInfo(globalPlayerId, findable.getDatabaseId(), date);
        if (info == null) {
            return;
        }
        foundInfos.put(findable.getDatabaseId(), info);
    }

    public void unfind(Findable findable, boolean group) {
        if (group) {
            unfindGroup(findable);
        } else {
            unfind(findable);
        }
    }

    public void unfindGroup(Findable findable) {
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(findable.getRoadMapId());
        if (roadMap == null) {
            return;
        }
        FindableGroup group = roadMap.getFindableGroup(findable);
        if (group == null) {
            return;
        }
        for (Findable n : group.getFindables()) {
            unfind(n.getDatabaseId());
        }
    }

    public void unfindNodes(RoadMap roadMap) {
        for (Findable findable : roadMap.getFindables()) {
            unfind(findable);
        }
    }

    public void unfind(Findable findable) {
        unfind(findable.getDatabaseId());
    }

    public void unfind(int nodeId) {
        DatabaseModel.getInstance().deleteFoundNode(globalPlayerId, nodeId);
        foundInfos.remove(nodeId);
    }

    public boolean hasFound(int nodeId) {
        return hasFound(foundInfos.values(), nodeId);
    }

    public boolean hasFound(Collection<FoundInfo> infos, int nodeId) {
        for (FoundInfo info : infos) {
            if (info.getNodeId() == nodeId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wie viele FoundInfo Objekte der Spieler zu einer Roadmap hat
     */
    public int getFoundAmount(RoadMap roadMap) {
        Collection<Integer> ids = roadMap.getFindables().stream().map(Findable::getDatabaseId).collect(Collectors.toSet());
        return (int) foundInfos.values().stream()
                .filter(fi -> ids.contains(fi.getNodeId()))
                .count();
    }

    public void setPath(Node targetNode) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        setPath(player, targetNode);
    }

    public void setPath(final Player player, final Findable target) {
        final PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (pathPlayer == null) {
            return;
        }
        final PlayerFindable playerFindable = new PlayerFindable(player, target.getRoadMap());
        PluginUtils.getInstance().runAsync(() -> AStarUtils.startPath(pathPlayer, playerFindable, target, false));
    }

    public void setPath(@NotNull ParticlePath path) {
        ParticlePath active = activePaths.get(path.getRoadMap().getDatabaseId());
        if (active != null) {
            active.cancel();
        }
        path.run(uuid);
        activePaths.put(path.getRoadMap().getDatabaseId(), path);
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
        ParticlePath toBeCancelled = activePaths.get(roadMap.getDatabaseId());
        if (toBeCancelled == null) {
            return;
        }

        toBeCancelled.cancel();
        activePaths.remove(roadMap.getDatabaseId());
    }

    public void pauseActivePath(RoadMap roadMap) {
        ParticlePath active = activePaths.get(roadMap.getDatabaseId());
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
        ParticlePath paused = activePaths.get(roadMap.getDatabaseId());
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
}
