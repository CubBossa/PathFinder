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

    public Collection<RoadMap> getRoadMaps() {
        return storedRoadMapsByID.values();
    }

    public @Nullable
    RoadMap getRoadMap(int databaseId) {
        return storedRoadMapsByID.get(databaseId);
    }

    public @Nullable
    RoadMap getRoadMap(String name) {
        for (RoadMap roadMap : storedRoadMapsByID.values()) {
            if (roadMap.getName().equals(name)) {
                return roadMap;
            }
        }
        return null;
    }

    public Collection<World> getRoadMapWorlds() {
        Collection<World> worlds = new ArrayList<World>();
        for (RoadMap roadMap : storedRoadMapsByID.values()) {
            worlds.add(roadMap.getWorld());
        }
        return worlds;
    }

    public Collection<RoadMap> getRoadMaps(World world) {
        Collection<RoadMap> roadMaps = new ArrayList<RoadMap>();
        for (RoadMap roadMap : storedRoadMapsByID.values()) {
            if (roadMap.getWorld().equals(world)) {
                roadMaps.add(roadMap);
            }
        }
        return roadMaps;
    }

    public Stream<RoadMap> getRoadMapsStream() {
        return storedRoadMapsByID.values().stream();
    }

    public Collection<RoadMap> getRoadMapsFindable(World world) {
        Collection<RoadMap> findableRoadMaps = new ArrayList<RoadMap>();
        for (RoadMap roadMap : getRoadMaps(world)) {
            if (roadMap.isFindableNodes()) {
                findableRoadMaps.add(roadMap);
            }
        }
        return findableRoadMaps;
    }

    public boolean isNameUnique(String name) {
        for (RoadMap roadMap : storedRoadMapsByID.values()) {
            if (roadMap.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }
}
