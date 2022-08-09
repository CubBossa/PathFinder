package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapEditor;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.module.visualizing.ParticlePath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@Getter
@Setter
public class PathPlayer {

    private final UUID uuid;

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
    }

    public void setPath(@NotNull ParticlePath path) {
        ParticlePath active = activePaths.get(path.getRoadMap().getKey());
        if (active != null) {
            active.cancel();
        }
        path.run(uuid);
        activePaths.put(path.getRoadMap().getKey(), path);
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

    /*public void setVisualizer(RoadMap roadMap, SimpleCurveVisualizer simpleCurveVisualizer) {
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
            return roadMap.getVisualizer();
        }
        Integer id = map.get(roadMap.getKey());
        if (id == null) {
            return roadMap.getVisualizer();
        }
        return VisualizerHandler.getInstance().getPathVisualizer(id);
    }*/
}
