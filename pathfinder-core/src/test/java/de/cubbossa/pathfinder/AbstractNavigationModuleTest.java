package de.cubbossa.pathfinder;

import org.junit.jupiter.api.Test;

class AbstractNavigationModuleTest extends PathFinderTest {

  @Test
  void registerFindPredicate() {
  }

  @Test
  void canFind() {
  }

  @Test
  void filterFindables() {
  }

  /*@Test
  void findPath() {
    setupPathFinder();

    Waypoint a = makeWaypoint(new Location(0, 0, 0, world));
    Waypoint b = makeWaypoint(new Location(0, 0, 10, world));
    Waypoint c = makeWaypoint(new Location(10, 0, 0, world));
    makeEdge(a, b);
    makeEdge(b, a);
    makeEdge(b, c);
    makeEdge(c, b);
    makeEdge(a, c);
    makeEdge(c, a);
    assertEdge(a.getNodeId(), b.getNodeId());

    AbstractNavigationModule<Object> nav = new AbstractNavigationModule<>();
    PathPlayer<Object> player = new TestPlayer();

    nav.findPath(player, AbstractNavigationModule.NavigateLocationImpl.staticLocation(
        new Location(-10, 0, -10, world)
    ), Set.of(AbstractNavigationModule.NavigateLocationImpl.staticLocation(
        new Location(10, 0, 10, world)
    )));

    shutdownPathFinder();
  }*/
}