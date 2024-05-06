package de.cubbossa.pathfinder.visualizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.misc.Location;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractParticlePlayerTest extends PathFinderTest {

  @RequiredArgsConstructor
  @Getter
  private class TestWorld implements de.cubbossa.pathfinder.misc.World {

    private final UUID uniqueId;

    @Override
    public String getName() {
      return "any";
    }
  }

  @AllArgsConstructor
  private class Loc {
    private UUID uuid;
    private double x, y, z;
  }

  private class TestParticlePlayer extends AbstractParticlePlayer<Loc> {

    Collection<UUID> played;

    public TestParticlePlayer(Collection<UUID> played, List<Location> path) {
      super(path);
      this.played = played;
    }

    @Override
    Location getView() {
      return new Location(0, 0, 0, new TestWorld(UUID.randomUUID()));
    }

    @Override
    void playParticle(Loc location) {
      played.add(location.uuid);
    }

    @Override
    Loc convert(Location location) {
      return new Loc(location.getWorld().getUniqueId(), location.getX(), location.getY(), location.getZ());
    }

    @Override
    Location convert(Loc location) {
      return new Location(location.x, location.y, location.z, new TestWorld(location.uuid));
    }
  }

  @BeforeEach
  void beforeAll() {
    setupPathFinder();
  }

  @Test
  void setNewestPathAndConvert() {
  }

  @Test
  void run() {
    List<UUID> played = new ArrayList<>();
    Location l1 = new Location(0, 0, 0, new TestWorld(UUID.randomUUID()));
    Location l2 = new Location(0, 0, 0, new TestWorld(UUID.randomUUID()));
    Location l3 = new Location(10_000, 0, 0, new TestWorld(UUID.randomUUID()));
    TestParticlePlayer player = new TestParticlePlayer(played, List.of(l1, l2, l3));
    player.setSteps(3);

    player.run();
    assertEquals(1, played.size());

    player.run();
    assertEquals(2, played.size());
    assertNotEquals(played.get(0), played.get(1));

    player.run();
    assertEquals(2, played.size());
  }
}