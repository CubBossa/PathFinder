package de.bossascrew.pathfinder.handler;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoadMapHandler {

    @Getter
    private static RoadMapHandler instance;

    private static Map<Integer, RoadMap> storedRoadMapsByID;

    public RoadMapHandler() {
        instance = this;
        storedRoadMapsByID = DatabaseModel.getInstance().loadRoadMaps();
    }

    public @Nullable
    RoadMap createRoadMap(String name, World world, boolean findableNodes) {
        RoadMap rm = DatabaseModel.getInstance().createRoadMap(name, world, findableNodes);
        if (rm == null) {
            return null;
        }
        storedRoadMapsByID.put(rm.getDatabaseId(), rm);
        return rm;
    }

    public void deleteRoadMap(RoadMap roadMap) {
        storedRoadMapsByID.remove(roadMap.getDatabaseId());
        PluginUtils.getInstance().runAsync(roadMap::delete);
    }

    public void cancelAllEditModes() {
        for(RoadMap roadMap : getRoadMaps()) {
            roadMap.cancelEditModes();
        }
    }

    public Collection<RoadMap> getRoadMaps() {
        return storedRoadMapsByID.values();
    }

    public @Nullable
    RoadMap getRoadMap(int databaseId) {
        return storedRoadMapsByID.get(databaseId);
    }

    public @Nullable
    RoadMap getRoadMap(String name) {
        return getRoadMapsStream().filter(rm -> rm.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Collection<World> getRoadMapWorlds() {
        return getRoadMapsStream().map(RoadMap::getWorld).collect(Collectors.toSet());
    }

    public Collection<RoadMap> getRoadMaps(World world) {
        return getRoadMapsStream().filter(roadMap -> roadMap.getWorld().equals(world)).collect(Collectors.toSet());
    }

    public Stream<RoadMap> getRoadMapsStream() {
        return storedRoadMapsByID.values().stream();
    }

    public Collection<RoadMap> getRoadMapsFindable(World world) {
        return getRoadMapsStream().filter(RoadMap::isFindableNodes).filter(roadMap -> roadMap.getWorld().equals(world)).collect(Collectors.toSet());
    }

    public boolean isNameUnique(String name) {
        return getRoadMapsStream().map(RoadMap::getName).noneMatch(rmName -> rmName.equalsIgnoreCase(name));
    }
}
