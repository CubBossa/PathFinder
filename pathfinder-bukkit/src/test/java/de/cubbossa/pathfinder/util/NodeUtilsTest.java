package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.TestNode;
import de.cubbossa.splinelib.util.BezierVector;
import de.cubbossa.splinelib.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeUtilsTest extends PathFinderTest {

  @Test
  void toBezierVector() {

    TestNode a = new TestNode(UUID.randomUUID());
    TestNode b = new TestNode(UUID.randomUUID());
    TestNode c = new TestNode(UUID.randomUUID());

    World world = new WorldImpl(UUID.randomUUID());
    a.setLocation(new Location(0, 0, 0, world));
    b.setLocation(new Location(2, 0, 0, new WorldImpl(UUID.randomUUID())));
    c.setLocation(new Location(4, 0, 0, new WorldImpl(UUID.randomUUID())));

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

  @Test
  void toSplineNew() {
    LinkedHashMap<Node, Double> path = new LinkedHashMap<>();
    path.put(new TestNode(UUID.randomUUID(), new Location(0, 0, 0, world)), 1d);
    path.put(new TestNode(UUID.randomUUID(), new Location(1, 0, 0, world)), 1d);
    path.put(new TestNode(UUID.randomUUID(), new Location(1, 0, 0, world)), 1d);
    path.put(new TestNode(UUID.randomUUID(), new Location(1, 1, 0, world)), 1d);
    path.put(new TestNode(UUID.randomUUID(), new Location(0, 1, 0, world)), 1d);

    List<BezierVector> a = NodeUtils.toSpline(path, true);
    List<BezierVector> b = NodeUtils.toSplineNew(path, true);

    Assertions.assertEquals(a, b);
  }

  @Test
  void toSpline() {
  }
}