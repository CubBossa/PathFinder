package de.cubbossa.pathfinder.util.location;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathPluginConfig.NearestLocationSolverConfig;
import de.cubbossa.pathfinder.PathPluginConfig.RaycastLocationWeightSolverConfig;
import de.cubbossa.pathfinder.PathPluginConfig.SimpleLocationWeightSolverConfig;
import de.cubbossa.pathfinder.util.VectorUtils;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class LocationWeightSolverPreset<C> {

  public static final LocationWeightSolverPreset<SimpleLocationWeightSolverConfig> SIMPLE =
      new LocationWeightSolverPreset<>("SIMPLE", config -> new SimpleLocationWeightSolver<>(
          config.connectionCount,
          n -> {
            Location l = n.getLocation();
            return new SimpleLocationWeightSolver.Position(l.getX(), l.getY(), l.getZ());
          }));

  public static final LocationWeightSolverPreset<RaycastLocationWeightSolverConfig> RAYCAST =
      new LocationWeightSolverPreset<>("RAYCAST",
          config -> new RaycastLocationWeightSolver<Node<?>>(
              n -> VectorUtils.toBukkit(n.getLocation()))
              .withRaycastCount(config.raycastCount)
              .withBlockCollisionWeight(config.blockCollisionWeight)
              .withStartLocationDirectionWeight(config.startLocationDirectionWeight)
              .withScopeLocationDirectionWeight(config.scopeLocationDirectionWeight));

  private static final LocationWeightSolverPreset<?>[] ALL = {
      SIMPLE, RAYCAST
  };

  private final String key;
  @Getter
  private final Function<C, LocationWeightSolver<Node<?>>> solverFunction;

  private LocationWeightSolverPreset(String key,
                                     Function<C, LocationWeightSolver<Node<?>>> solverFunction) {
    this.key = key;
    this.solverFunction = solverFunction;
  }

  public static <T> LocationWeightSolver<T> fromConfig(NearestLocationSolverConfig config) {
    LocationWeightSolverPreset<?> preset = config.algorithm.preset;
    if (preset.equals(SIMPLE)) {
      return (LocationWeightSolver<T>) SIMPLE.getSolverFunction().apply(config.simpleConfig);
    }
    return (LocationWeightSolver<T>) RAYCAST.getSolverFunction().apply(config.raycastConfig);
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
