package de.cubbossa.pathfinder.util.location;

import de.cubbossa.pathapi.misc.LocationWeightSolver;
import de.cubbossa.pathfinder.util.Triple;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@AllArgsConstructor
@With
public class RaycastLocationWeightSolver<T> implements LocationWeightSolver<T> {

  private final Function<T, Location> mapper;
  private int raycastCount = 10;
  private double blockCollisionWeight = 10_000d;
  private double startLocationDirectionWeight = 1;
  private double scopeLocationDirectionWeight = 0;

  @Override
  public Map<T, Double> solve(T location, Iterable<T> scope) {
    Location startLocation = mapper.apply(location);
    startLocation = startLocation.add(
        startLocation.getDirection().normalize().multiply(startLocationDirectionWeight));
    final Location fStartLocation = startLocation;

    List<Triple<Node<T>, Double, Integer>> triples =
        StreamSupport.stream(scope.spliterator(), false)
            .map(element -> {
              Location loc = mapper.apply(element);
              if (scopeLocationDirectionWeight != 0) {
                loc =
                    loc.add(loc.getDirection().normalize().multiply(scopeLocationDirectionWeight));
              }
              return new Node<>(element, loc);
            })
            .filter(e -> Objects.equals(e.location().getWorld(), fStartLocation.getWorld()))
            // make them sortable by cheap distance attribute
            .map(
                e -> new AbstractMap.SimpleEntry<>(e, e.location().distanceSquared(fStartLocation)))
            .sequential()
            // sort them on squared distance
            .sorted(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
            // only look at closest 10 elements
            .limit(raycastCount)
            .map(e -> {
              Node<T> n = e.getKey();
              Vector dir = n.location().toVector().clone()
                  .add(new Vector(0, .5f, 0))
                  .subtract(fStartLocation.toVector());
              double length = dir.length();
              dir.normalize();
              Location loc = fStartLocation.clone().setDirection(dir);
              int count = 1;

              BlockIterator iterator = new BlockIterator(loc, 0, (int) length);
              int cancel = 0; // sometimes the while loop does not cancel without extra counter
              while (iterator.hasNext() && cancel++ < 100) {
                Block block = iterator.next();
                if (block.getType().isBlock() && block.getType().isSolid()) {
                  count++;
                }
              }
              return Triple.of(n, length, count);
            }).toList();

    boolean anyNullCount = triples.stream().anyMatch(e -> e.getRight() == 1);

    Map<T, Double> result = new HashMap<>();
    triples.stream()
        .filter(e -> !anyNullCount || e.getRight() == 1)
        .forEach(e -> result.put(e.getLeft().element(),
            e.getMiddle() * e.getRight() * blockCollisionWeight));
    return result;
  }

  record Node<T>(T element, Location location) {
  }
}
