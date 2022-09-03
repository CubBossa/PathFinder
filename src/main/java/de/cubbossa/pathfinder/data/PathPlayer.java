package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.events.roadmap.RoadmapSelectEvent;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapEditor;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PathPlayer {

    private final UUID uuid;

    private final Map<NamespacedKey, VisualizerPath> activePaths;

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
        RoadmapSelectEvent event = new RoadmapSelectEvent(Bukkit.getPlayer(uuid), selectedRoadMap, key);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        deselectRoadMap(false);
        this.selectedRoadMap = key;
    }

    public void deselectRoadMap() {
        deselectRoadMap(true);
    }

    private void deselectRoadMap(boolean callEvent) {
        if (callEvent) {
            RoadmapSelectEvent event = new RoadmapSelectEvent(Bukkit.getPlayer(uuid), selectedRoadMap, null);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
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

    /*public void setVisualizer(RoadMap roadMap, ParticleVisualizer ParticleVisualizer) {
        Map<Integer, Integer> map = VisualizerHandler.getInstance().getPlayerVisualizers().getOrDefault(globalPlayerId, new HashMap<>());
        if (map.containsKey(roadMap.getKey())) {
            SqlStorage.getInstance().updatePlayerVisualizer(globalPlayerId, roadMap, ParticleVisualizer);
        } else {
            SqlStorage.getInstance().createPlayerVisualizer(globalPlayerId, roadMap, ParticleVisualizer);
        }
        map.put(roadMap.getKey(), ParticleVisualizer.getDatabaseId());
        VisualizerHandler.getInstance().getPlayerVisualizers().put(globalPlayerId, map);

        if (activePaths.containsKey(roadMap.getKey())) {
            activePaths.get(roadMap.getKey()).run();
        }
    }

    public ParticleVisualizer getVisualizer(RoadMap roadMap) {
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
