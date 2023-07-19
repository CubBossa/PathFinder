package de.cubbossa.pathfinder.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Version implements Comparable<Version> {

  /**
   * Valid version patterns:
   * - v1
   * - v2.1
   * - 2.1-b1
   * - v2.1.19.20-b109
   * - v2.1.19.20-SNAPSHOT-b109
   */
  private static final Pattern VERSION_PATTERN =
      Pattern.compile("v?([0-9]+(\\.[0-9]+)*)(-SNAPSHOT)?(-b?([0-9]+))?");

  private final int[] elements;
  private final boolean snapshot;
  private final Integer build;

  public Version(String versionString) {

    Matcher matcher = VERSION_PATTERN.matcher(versionString);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid version string: '" + versionString + "'.");
    }
    elements = Arrays.stream(matcher.group(1).split("\\."))
        .mapToInt(Integer::parseInt)
        .toArray();
    snapshot = matcher.group(4) != null && matcher.group(4).equalsIgnoreCase("snapshot");
    build = matcher.group(matcher.groupCount() - 1) == null ? null : Integer.valueOf(
        matcher.group(matcher.groupCount() - 1).substring(1).replaceAll("[a-z]", ""));
  }

  @Override
  public String toString() {
    String version = "v" + Arrays.stream(elements).mapToObj(value -> value + "")
        .collect(Collectors.joining("."));
    if (snapshot) {
      version += "-SNAPSHOT";
    }
    if (build != null) {
      version += "-b" + build;
    }
    return version;
  }

  @Override
  public int compareTo(@NotNull Version o) {
    int base = compareBase(o);
    if (base == 0) {
      int snapshot = compareSnapshot(o);
      if (snapshot == 0) {
        return compareBuild(o);
      }
      return snapshot;
    }
    return base;
  }

  private int compareBase(Version o) {
    int compareIndex = 0;
    while (true) {
      if (o.elements.length == elements.length && o.elements.length <= compareIndex) {
        return 0;
      }
      if (o.elements.length <= compareIndex) {
        return 1;
      }
      if (elements.length <= compareIndex) {
        return -1;
      }
      int comp = Integer.compare(elements[compareIndex], o.elements[compareIndex]);
      if (comp != 0) {
        return comp;
      }
      compareIndex++;
    }
  }

  private int compareSnapshot(Version o) {
    return Boolean.compare(snapshot, o.snapshot);
  }

  private int compareBuild(Version o) {
    return Integer.compare(build == null ? 0 : build, o.build == null ? 0 : o.build);
  }
}
