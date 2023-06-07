package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorUtilsTest {

  @Test
  void snapEven() {

    Assertions.assertEquals(
        new Vector(0.5, 0, 1),
        VectorUtils.snap(new Vector(0.3, 0, 0.9999), 2)
    );
    Assertions.assertEquals(
        new Vector(0.25, 0.25, 0.75),
        VectorUtils.snap(new Vector(0.3, 0, 0.9999), 2, new Vector(.25, .25, .25))
    );
    Assertions.assertEquals(
        new Vector(-0.25, 0.25, -0.75),
        VectorUtils.snap(new Vector(-0.3, 0, -0.9999), 2, new Vector(.25, .25, .25))
    );
  }

  @Test
  void snapUneven() {

    Assertions.assertEquals(
        new Vector(1 / 3., 0, 0),
        VectorUtils.snap(new Vector(0.26, 0, 0), 3)
    );
  }
}