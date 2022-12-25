package de.cubbossa.pathfinder.module.maze;

import de.cubbossa.menuframework.util.Pair;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class RoadMapMazePattern extends MazePattern {

  private int spacing = 2;
  private RoadMap roadMap;
  private Map<Integer, Node> southOpenQueue = new HashMap<>();
  private Map<Integer, Node> eastOpenQueue = new HashMap<>();

  private RoadMap.RoadMapBatchEditor batchEditor;
  private Collection<Pair<Node, Node>> edges;

  public RoadMapMazePattern(RoadMap roadMap) {
    this.roadMap = roadMap;
  }

  @Override
  void start() {
    batchEditor = roadMap.getBatchEditor();
  }

  @Override
  void complete() {
    batchEditor.commit();
  }

  void place(Location location, boolean northBlocked, boolean eastBlocked, boolean southBlocked,
             boolean westBlocked) {
    location = location.getBlock().getLocation().add(.5, 1.5, .5);
    int bz = location.getBlockZ();
    int bx = location.getBlockX();

    Node node = roadMap.createWaypoint(location, true);

    Node southOpen = southOpenQueue.get(bx);
    if (southOpen != null) {
      if (!northBlocked) {
        roadMap.connectNodes(southOpen, node, false);
      }
      southOpenQueue.remove(bx);
    }
    if (!southBlocked) {
      southOpenQueue.put(bx, node);
    }

    Node eastOpen = eastOpenQueue.get(bz);
    if (eastOpen != null) {
      if (!westBlocked) {
        roadMap.connectNodes(eastOpen, node, false);
      }
      eastOpenQueue.remove(bz);
    }
    if (!eastBlocked) {
      eastOpenQueue.put(bz, node);
    }
  }

  @Override
  void placeNone(Location location) {
    // not relevant
  }

  @Override
  void placeNorth(Location location) {
    place(location, false, true, true, true);
  }

  @Override
  void placeNorthEast(Location location) {
    place(location, false, false, true, true);
  }

  @Override
  void placeNorthEastSouth(Location location) {
    place(location, false, false, false, true);
  }

  @Override
  void placeNorthEastSouthWest(Location location) {
    place(location, false, false, false, false);
  }

  @Override
  void placeNorthEastWest(Location location) {
    place(location, false, false, true, false);
  }

  @Override
  void placeNorthSouth(Location location) {
    // straight
  }

  @Override
  void placeNorthSouthWest(Location location) {
    place(location, false, true, false, false);
  }

  @Override
  void placeNorthWest(Location location) {
    place(location, false, true, true, false);
  }

  @Override
  void placeEast(Location location) {
    place(location, true, false, true, true);
  }

  @Override
  void placeEastSouth(Location location) {
    place(location, true, false, false, true);
  }

  @Override
  void placeEastSouthWest(Location location) {
    place(location, true, false, false, false);
  }

  @Override
  void placeEastWest(Location location) {
    // straight
  }

  @Override
  void placeSouth(Location location) {
    place(location, true, true, false, true);
  }

  @Override
  void placeSouthWest(Location location) {
    place(location, true, true, false, false);
  }

  @Override
  void placeWest(Location location) {
    place(location, true, true, true, false);
  }
}
