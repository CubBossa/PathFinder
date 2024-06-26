package de.cubbossa.pathfinder.util;

import java.awt.Color;

public class LerpUtils {

  public static Color lerp(Color a, Color b, double percent) {
    percent = Double.max(0, Double.min(1, percent));
    int red = lerp(a.getRed(), b.getRed(), percent);
    int blue = lerp(a.getBlue(), b.getBlue(), percent);
    int green = lerp(a.getGreen(), b.getGreen(), percent);
    return new Color(red, green, blue);
  }

  public static Color lerp(org.bukkit.Color a, org.bukkit.Color b, double percent) {
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
