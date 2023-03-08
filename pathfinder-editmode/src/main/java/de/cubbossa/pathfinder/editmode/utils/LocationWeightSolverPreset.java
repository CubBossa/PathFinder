package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.core.node.Node;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor
public enum LocationWeightSolverPreset {

  SIMPLE(data -> new SimpleLocationWeightSolver<>(n -> {
    Location l = n.getLocation();
    return new SimpleLocationWeightSolver.Position(l.getX(), l.getY(), l.getZ());
  })),
  RAYCAST(data -> new RaycastLocationWeightSolver<>(Node::getLocation)
      .withRaycastCount((Integer) data.getOrDefault("raycast-count", 10))
      .withBlockCollisionWeight((Double) data.getOrDefault("block-collision-weight", 10_000d))
      .withStartLocationDirectionWeight(
          (Double) data.getOrDefault("start-location-direction-weight", 1d))
      .withScopeLocationDirectionWeight(
          (Double) data.getOrDefault("scope-location-direction-weight", 0d)))
  ;

  @Getter
  private final Function<Map<String, Object>, LocationWeightSolver<Node>> solverFunction;
}
