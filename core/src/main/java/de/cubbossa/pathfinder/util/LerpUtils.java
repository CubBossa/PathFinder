package de.cubbossa.pathfinder.util;

import java.awt.*;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LerpUtils {

  public static Location lerp(Location a, Location b, double percent) {
    if (!Objects.equals(a.getWorld(), b.getWorld())) {
      throw new IllegalArgumentException("Both locations must be in the same world to be lerped.");
    }
    return lerp(a.toVector(), b.toVector(), percent).toLocation(a.getWorld());
  }

  public static Vector lerp(Vector a, Vector b, double percent) {
    return a.clone().add(b.clone().subtract(a).multiply(percent));
  }

  public static Color lerp(Color a, Color b, double percent) {
    percent = Double.max(0, Double.min(1, percent));
    int red = lerp(a.getRed(), b.getRed(), percent);
    int blue = lerp(a.getBlue(), b.getBlue(), percent);
    int green = lerp(a.getGreen(), b.getGreen(), percent);
    return new Color(red, green, blue);
  }

  public static int lerp(int a, int b, double percent) {
    return (int) ((1 - percent) * a + percent * b);
  }
}
