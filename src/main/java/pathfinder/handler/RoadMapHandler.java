package pathfinder.handler;

import lombok.Getter;
import org.bukkit.World;
import pathfinder.RoadMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoadMapHandler {

    @Getter
    private static RoadMapHandler instance;

    private static Map<Integer, RoadMap> storedRoadMapsByID;

    public RoadMapHandler() {
        instance = this;

        storedRoadMapsByID = null; //aus DatabaseModel laden
    }

    public void createRoadMap() {
        //TODO erstelle eine Roadmap
    }

    public void deleteRoadMap() {
        //TODO lösche eine RoadMap aus HashMap, Visualizer, zugehörige Nodes und aus Datenbank.
    }

    public void loadRoadMaps() {
        //TODO lade roadmaps aus der Datenbank
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
        for(RoadMap roadMap : storedRoadMapsByID.values()) {
            if(roadMap.getName().equals(name)) {
                return roadMap;
            }
        }
        return null;
    }

    public Collection<World> getRoadMapWorlds() {
        Collection<World> worlds = new ArrayList<World>();
        for(RoadMap roadMap : storedRoadMapsByID.values()) {
            worlds.add(roadMap.getWorld());
        }
        return worlds;
    }

    public Collection<RoadMap> getRoadMaps(World world) {
        Collection<RoadMap> roadMaps = new ArrayList<RoadMap>();
        for(RoadMap roadMap : storedRoadMapsByID.values()) {
            if(roadMap.getWorld().equals(world))
                roadMaps.add(roadMap);
        }
        return roadMaps;
    }

    public Collection<RoadMap> getRoadMapsFindable(World world) {
        Collection<RoadMap> findableRoadMaps = new ArrayList<RoadMap>();
        for(RoadMap roadMap : getRoadMaps(world)) {
            if(roadMap.isFindableNodes()) {
                findableRoadMaps.add(roadMap);
            }
        }
        return findableRoadMaps;
    }
}
