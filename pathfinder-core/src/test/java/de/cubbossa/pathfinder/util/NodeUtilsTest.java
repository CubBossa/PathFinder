package de.cubbossa.pathfinder.util;

import static org.junit.jupiter.api.Assertions.*;

import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.test.TestNode;
import de.cubbossa.splinelib.util.BezierVector;
import de.cubbossa.splinelib.util.Vector;
import org.bukkit.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeUtilsTest {

  @Test
  void toBezierVector() {

    TestNode a = new TestNode(1, null);
    TestNode b = new TestNode(1, null);
    TestNode c = new TestNode(1, null);

    a.setLocation(new Location(null, 0, 0, 0));
    b.setLocation(new Location(null, 2, 0, 0));
    c.setLocation(new Location(null, 4, 0, 0));

    assertEquals(
        NodeUtils.toBezierVector(a, b, c, 2, 2, 2, new NodeUtils.TangentModifier(1, 0)),
        new BezierVector(new Vector(2, 0, 0), new Vector(1, 0, 0), new Vector(3, 0, 0))
    );

    assertEquals(
        NodeUtils.toBezierVector(a, b, c, 0, 2, 2, new NodeUtils.TangentModifier(1, 0)),
        new BezierVector(new Vector(2, 0, 0), new Vector(1, 0, 0), new Vector(3, 0, 0))
    );

    assertEquals(
        NodeUtils.toBezierVector(a, b, c, 0, 2, 0, new NodeUtils.TangentModifier(1, 0)),
        new BezierVector(new Vector(2, 0, 0), new Vector(0, 0, 0), new Vector(4, 0, 0))
    );
  }
}