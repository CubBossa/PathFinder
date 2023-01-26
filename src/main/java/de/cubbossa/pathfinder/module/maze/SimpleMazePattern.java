package de.cubbossa.pathfinder.module.maze;

import de.cubbossa.pathfinder.PathPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

@Getter
@Setter
public class SimpleMazePattern extends MazePattern {

  private Material floorType = Material.SPRUCE_PLANKS;
  private Material wallType = Material.DEEPSLATE;
  private int spacing = 2;

  public void place(Location location, boolean north, boolean east, boolean south, boolean west) {
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {

      World w = location.getWorld();
      assert w != null;
      w.getBlockAt(location.clone().add(1, 1, 1)).setType(wallType);
      w.getBlockAt(location.clone().add(-1, 1, 1)).setType(wallType);
      w.getBlockAt(location.clone().add(1, 1, -1)).setType(wallType);
      w.getBlockAt(location.clone().add(-1, 1, -1)).setType(wallType);
      if (north) {
        w.getBlockAt(location.clone().add(0, 1, -1)).setType(wallType);
      }
      if (east) {
        w.getBlockAt(location.clone().add(1, 1, 0)).setType(wallType);
      }
      if (south) {
        w.getBlockAt(location.clone().add(0, 1, 1)).setType(wallType);
      }
      if (west) {
        w.getBlockAt(location.clone().add(-1, 1, 0)).setType(wallType);
      }
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          w.getBlockAt(location.clone().add(i, 0, j)).setType(floorType);
        }
      }
    });
  }

  @Override
  void placeNone(Location location) {
    place(location, true, true, true, true);
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
    place(location, false, true, false, true);
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
    place(location, true, false, true, false);
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

  @Override
  void start() {

  }

  @Override
  void complete() {

  }
}
