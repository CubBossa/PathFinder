package de.bossascrew.pathfinder.data;

import de.bossascrew.core.player.PlayerHandler;
import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
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
    private final int globalPlayerId;
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
    private final Map<Integer, Findable> lastSetFindables;

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

        foundFindables = DatabaseModel.getInstance().loadFoundNodes(globalPlayerId, false);
        foundGroups = DatabaseModel.getInstance().loadFoundNodes(globalPlayerId, true);
    }

    public void find(Findable findable, boolean group, Date date) {
        if (group) {
            if (findable.getGroup() != null) {
                find(findable.getGroup().getDatabaseId(), true, date);
            }
        } else {
            find(findable.getDatabaseId(), false, date);
        }
    }

    public void find(int id, boolean group, Date date) {
        if (hasFound(id, group)) {
            return;
        }
        FoundInfo info = DatabaseModel.getInstance().newFoundInfo(globalPlayerId, id, group, date);
        if (group) {
            foundGroups.put(id, info);
        } else {
            foundFindables.put(id, info);
        }
    }

    public void unfind(Findable findable, boolean group) {
        unfind(group ? findable.getNodeGroupId() : findable.getDatabaseId(), group);
    }

    public void unfind(RoadMap roadMap) {
        for (FindableGroup group : roadMap.getGroups().values()) {
            unfind(group.getDatabaseId(), true);
        }
        for (Findable findable : roadMap.getFindables()) {
            unfind(findable.getDatabaseId(), false);
        }
    }

    public void unfind(int id, boolean group) {
        if(group) {
            foundGroups.remove(id);
        } else {
            foundFindables.remove(id);
        }
        DatabaseModel.getInstance().deleteFoundNode(globalPlayerId, id, group);
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

    public boolean hasFound(Findable findable) {
        if (findable.getGroup() != null) {
            return foundGroups.containsKey(findable.getGroup().getDatabaseId());
        }
        return foundFindables.containsKey(findable.getDatabaseId());
    }

    /**
     * Wie viele FoundInfo Objekte der Spieler zu einer Roadmap hat
     */
    public int getFoundAmount(RoadMap roadMap) {
        Collection<Integer> ids = roadMap.getFindables().stream()
                .filter(f -> f.getGroup() == null)
                .filter(this::hasFound)
                .map(Findable::getDatabaseId)
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

        Map<Integer, AtomicBoolean> lock = PlayerListener.getHasFoundTarget().getOrDefault(uuid, new ConcurrentHashMap<>());
        lock.put(path.getRoadMap().getDatabaseId(), new AtomicBoolean(false));
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

    public void setLastSetGroup(FindableGroup findableGroup) {
        this.lastSetGroups.put(findableGroup.getRoadMap().getDatabaseId(), findableGroup);
    }

    public @Nullable FindableGroup getLastSetGroup(RoadMap roadMap) {
        return this.lastSetGroups.get(roadMap.getDatabaseId());
    }

    public void setLastSetFindable(Findable findable) {
        this.lastSetFindables.put(findable.getRoadMapId(), findable);
    }

    public @Nullable Findable getLastSetFindable(RoadMap roadMap) {
        return this.lastSetFindables.get(roadMap.getDatabaseId());
    }
}
