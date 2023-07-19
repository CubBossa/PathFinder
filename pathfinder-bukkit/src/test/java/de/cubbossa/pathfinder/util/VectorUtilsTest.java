package de.cubbossa.pathfinder.util;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorUtilsTest {

  @Test
  void distancePointToLine1() {

    Assertions.assertEquals(3, BukkitVectorUtils.distancePointToLine(
        new Vector(2.78123, 3, 0),
        new Vector(0, 0, 0),
        new Vector(1, 0, 0)
    ), 0.000001);
  }

  @Test
  void distancePointToLine2() {
    Assertions.assertEquals(0, BukkitVectorUtils.distancePointToLine(
        new Vector(2.78123, 0, 0),
        new Vector(0, 0, 0),
        new Vector(1, 0, 0)
    ), 0.000001);
  }

  @Test
  void distancePointToLine3() {
    Assertions.assertEquals(12.1293, BukkitVectorUtils.distancePointToLine(
        new Vector(27, 12.1293, 0),
        new Vector(0, 0, 0),
        new Vector(29, 0, 0)
    ), 0.000001);
  }

  @Test
  void distancePointToLine4() {
    Assertions.assertEquals(70.710, BukkitVectorUtils.distancePointToLine(
        new Vector(100, 13, 100),
        new Vector(100, 13, 0),
        new Vector(0, 13, 100)
    ), 0.01);
  }

  @Test
  void closestPointOnLine1() {
    Assertions.assertEquals(new Vector(0, 0, 0), BukkitVectorUtils.closestPointOnSegment(
        new Vector(0, 1, 0),
        new Vector(-1, 0, 0),
        new Vector(1, 0, 0)
    ));
  }

  @Test
  void closestPointOnLine2() {
    Assertions.assertEquals(new Vector(-1, 0, 0), BukkitVectorUtils.closestPointOnSegment(
        new Vector(-2, 0, 0),
        new Vector(-1, 0, 0),
        new Vector(1, 0, 0)
    ));
  }

  @Test
  void closestPointOnLine3() {
    Assertions.assertEquals(new Vector(1, 0, 0), BukkitVectorUtils.closestPointOnSegment(
        new Vector(2, 0, 0),
        new Vector(-1, 0, 0),
        new Vector(1, 0, 0)
    ));
  }
}