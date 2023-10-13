package de.cubbossa.pathfinder;

import static org.junit.jupiter.api.Assertions.*;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class AbstractNavigationHandlerTest extends PathFinderTest {

  @Test
  void registerFindPredicate() {
  }

  @Test
  void canFind() {
  }

  @Test
  void filterFindables() {
  }

  @Test
  @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
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

    AbstractNavigationHandler<Object> nav = new AbstractNavigationHandler<>();
    PathPlayer<Object> player = new TestPlayer();

    nav.findPath(player, AbstractNavigationHandler.CommonNavigateLocation.staticLocation(
        new Location(-10, 0, -10, world)
    ), Set.of(AbstractNavigationHandler.CommonNavigateLocation.staticLocation(
        new Location(10, 0, 10, world)
    )));

    shutdownPathFinder();
  }
}