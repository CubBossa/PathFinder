package de.cubbossa.pathfinder.module.maze;

import de.cubbossa.pathfinder.PathPlugin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Getter
@Setter
@AllArgsConstructor
public class MazeConverter {

  private Maze maze;

  public MazeConverter convertMaze(MazePattern pattern, Location startLocation) {
    Bukkit.getScheduler().runTaskAsynchronously(PathPlugin.getInstance(), () -> {
      for (int y = 0; y < maze.getHeight(); y++) {
        for (int x = 0; x < maze.getWidth(); x++) {
          placePattern(
              startLocation.clone().add(x * pattern.getSpacing(), 0, y * pattern.getSpacing()),
              pattern, maze.getGrid()[y][x]);
        }
      }
    });
    return this;
  }

  private void placePattern(Location location, MazePattern pattern, short mask) {
    switch (mask) {
      case 0 -> pattern.placeNone(location);
      case 1 -> pattern.placeNorth(location);
      case 1 + 2 -> pattern.placeNorthEast(location);
      case 1 + 2 + 4 -> pattern.placeNorthEastSouth(location);
      case 1 + 2 + 8 -> pattern.placeNorthEastWest(location);
      case 1 + 2 + 4 + 8 -> pattern.placeNorthEastSouthWest(location);
      case 1 + 4 -> pattern.placeNorthSouth(location);
      case 1 + 4 + 8 -> pattern.placeNorthSouthWest(location);
      case 1 + 8 -> pattern.placeNorthWest(location);
      case 2 -> pattern.placeEast(location);
      case 2 + 4 -> pattern.placeEastSouth(location);
      case 2 + 4 + 8 -> pattern.placeEastSouthWest(location);
      case 2 + 8 -> pattern.placeEastWest(location);
      case 4 -> pattern.placeSouth(location);
      case 4 + 8 -> pattern.placeSouthWest(location);
      case 8 -> pattern.placeWest(location);
    }
  }
}
