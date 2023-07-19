package de.cubbossa.pathfinder.util;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BukkitVectorUtilsTest {

  @Test
  void getIntersection() {
    Assertions.assertEquals(
        new Vector(300, 0, 0),
        BukkitVectorUtils.getIntersection(
            new Vector(300, -100, 0), new Vector(0, 1, 0),
            new Vector(0, 0, 0), new Vector(0, 1, 0)
        )
    );
    Assertions.assertEquals(
        new Vector(1, 0.5, 0),
        BukkitVectorUtils.getIntersection(
            new Vector(0, 0, 0), new Vector(2, 1, 0),
            new Vector(1, 0, 0), new Vector(1, 0, 0)
        )
    );
    Assertions.assertEquals(
        new Vector(-1, -0.5, 0),
        BukkitVectorUtils.getIntersection(
            new Vector(0, 0, 0), new Vector(-2, -1, 0),
            new Vector(-1, 0, 0), new Vector(1, 0, 0)
        )
    );
  }

  @Test
  void testGetIntersection() {
    Assertions.assertEquals(
        new BukkitVectorUtils.Orientation(new Vector(1, 0.5, 0), new Vector(-1, 0, 0)),
        BukkitVectorUtils.getIntersection(new Vector(0, 0, 0), new Vector(2, 1, 0), new Vector(1, 0, 0))
    );
    Assertions.assertEquals(
        new BukkitVectorUtils.Orientation(new Vector(-2.5, 3, 0), new Vector(0, -1, 0)),
        BukkitVectorUtils.getIntersection(new Vector(0, .5, 0), new Vector(-1, 1, 0), new Vector(-3, 3, 0))
    );
    Assertions.assertEquals(
        new BukkitVectorUtils.Orientation(new Vector(-2.5, -2.5, -2), new Vector(0, 1, 0)),
        BukkitVectorUtils.getIntersection(new Vector(0, 0, 0.5), new Vector(-1, -1, -1), new Vector(-3, -3, -3))
    );
  }
}