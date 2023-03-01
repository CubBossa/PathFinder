package de.cubbossa.pathfinder.module.maze;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Maze {

  private int height;
  private int width;
  private short[][] grid;
  public Maze(int sizeX, int sizeY) {
    this.width = sizeX;
    this.height = sizeY;
    this.grid = new short[height][width];

    carvePassageFrom(0, 0, grid);
  }

  public static void main(String[] args) {
    System.out.println(new Maze(60, 15));
  }

  private void carvePassageFrom(int cx, int cy, short[][] grid) {
    List<Direction> directions = Lists.newArrayList(Direction.values());
    Collections.shuffle(directions);

    for (Direction direction : directions) {
      int nx = cx + Direction.deltaX(direction);
      int ny = cy + Direction.deltaY(direction);

      if ((ny >= 0 && ny <= grid.length - 1) && (nx >= 0 && nx <= grid[ny].length - 1)
          && grid[ny][nx] == 0) {
        grid[cy][cx] |= direction.bit;
        grid[ny][nx] |= Direction.opposite(direction).bit;
        carvePassageFrom(nx, ny, grid);
      }
    }
  }

  public String toXString() {
    for (short[] s : grid) {
      System.out.println(Arrays.toString(s));
    }
    System.out.println(" " + "_".repeat(width * 2 - 1));
    for (int y = 0; y < height; y++) {
      System.out.print("|");
      for (int x = 0; x < width; x++) {
        if ((grid[y][x] & Direction.E.bit) != 0) {
          System.out.print((((grid[y][x] | grid[y][x + 1]) & Direction.S.bit) != 0) ? " " : "_");
        } else {
          System.out.print("|");
        }
      }
      System.out.print("\n");
    }
    return "";
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (short[] line : grid) {
      for (short s : line) {
        builder.append(toChar(s));
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  private char toChar(short mask) {
    return switch (mask) {
      case 1 -> '╵';
      case 1 + 2 -> '└';
      case 1 + 2 + 4 -> '├';
      case 1 + 2 + 8 -> '┴';
      case 1 + 2 + 4 + 8 -> '┼';
      case 1 + 4 -> '│';
      case 1 + 4 + 8 -> '┤';
      case 1 + 8 -> '┘';
      case 2 -> '╶';
      case 2 + 4 -> '┌';
      case 2 + 4 + 8 -> '┬';
      case 2 + 8 -> '─';
      case 4 -> '╷';
      case 4 + 8 -> '┐';
      case 8 -> '╴';
      default -> 'X';
    };
  }

  public enum Direction {
    N(1), E(2), S(4), W(8);

    private final short bit;

    Direction(int bit) {
      this.bit = (short) bit;
    }

    public static Collection<Direction> fromBits(short mask) {
      HashSet<Direction> set = new HashSet<>();
      for (int i = 0; i < 4; i++) {
        if (((i << mask) & 1) == 1) {
          set.add(Direction.values()[i]);
        }
      }
      return set;
    }

    public static short toBits(Collection<Direction> directions) {
      return (short) directions.stream().mapToInt(direction -> direction.bit).sum();
    }

    public static Direction opposite(Direction direction) {
      return switch (direction) {
        case E -> W;
        case W -> E;
        case N -> S;
        case S -> N;
      };
    }

    public static int deltaX(Direction direction) {
      return switch (direction) {
        case E -> 1;
        case W -> -1;
        case N, S -> 0;
      };
    }

    public static int deltaY(Direction direction) {
      return switch (direction) {
        case N -> -1;
        case S -> 1;
        case E, W -> 0;
      };
    }
  }

}
