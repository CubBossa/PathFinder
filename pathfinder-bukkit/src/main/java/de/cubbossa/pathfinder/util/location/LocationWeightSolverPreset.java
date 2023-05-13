package de.cubbossa.pathfinder.util.location;

import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.LocationWeightSolver;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.VectorUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

public class LocationWeightSolverPreset<C> {

  public static final LocationWeightSolverPreset<PathFinderConfig.SimpleLocationWeightSolverConfig> SIMPLE =
      new LocationWeightSolverPreset<>("SIMPLE", config -> new SimpleLocationWeightSolver<>(
          config.getConnectionCount(),
          n -> {
            Location l = n.getLocation();
            return new SimpleLocationWeightSolver.Position(l.getX(), l.getY(), l.getZ());
          }));

  public static final LocationWeightSolverPreset<PathFinderConfig.RaycastLocationWeightSolverConfig> RAYCAST =
      new LocationWeightSolverPreset<>("RAYCAST",
          config -> new RaycastLocationWeightSolver<Node>(
              n -> VectorUtils.toBukkit(n.getLocation()))
              .withRaycastCount(config.getRaycastCount())
              .withBlockCollisionWeight(config.getBlockCollisionWeight())
              .withStartLocationDirectionWeight(config.getStartLocationDirectionWeight())
              .withScopeLocationDirectionWeight(config.getScopeLocationDirectionWeight()));

  private static final LocationWeightSolverPreset<?>[] ALL = {
      SIMPLE, RAYCAST
  };

  private final String key;
  @Getter
  private final Function<C, LocationWeightSolver<Node>> solverFunction;

  private LocationWeightSolverPreset(String key,
                                     Function<C, LocationWeightSolver<Node>> solverFunction) {
    this.key = key;
    this.solverFunction = solverFunction;
  }

  public static <T> LocationWeightSolver<T> fromConfig(PathFinderConfig.NearestLocationSolverConfig config) {
    LocationWeightSolverPreset<?> preset = LocationWeightSolverPresetEnum.valueOf(config.getAlgorithm()).preset;
    if (preset.equals(SIMPLE)) {
      return (LocationWeightSolver<T>) SIMPLE.getSolverFunction().apply(config.getSimpleConfig());
    }
    return (LocationWeightSolver<T>) RAYCAST.getSolverFunction().apply(config.getRaycastConfig());
  }

  @RequiredArgsConstructor
  public enum LocationWeightSolverPresetEnum {
    SIMPLE(LocationWeightSolverPreset.SIMPLE),
    RAYCAST(LocationWeightSolverPreset.RAYCAST),
    ;
    @Getter
    private final LocationWeightSolverPreset<?> preset;
  }
}
