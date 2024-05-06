package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.Location;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AbstractParticleTrailPlayer<LocationT> implements Disposable {

  private final AbstractParticlePlayer<LocationT> owner;

  @Getter
  @Setter
  private int lowerBound = 0;
  @Getter
  @Setter
  private int upperBound = 0;
  private final Point<LocationT>[] points;

  public AbstractParticleTrailPlayer(AbstractParticlePlayer<LocationT> owner, List<Location> points) {
    this.owner = owner;
    this.points = new Point[points.size()];
    int i = 0;
    for (Location p : points) {
      this.points[i] = new Point<>(p, owner.convert(p), i++);
    }
    resetBounds();
  }

  public AbstractParticleTrailPlayer(AbstractParticleTrailPlayer<LocationT> other) {
    this.owner = other.owner;
    this.points = Arrays.copyOf(other.points, other.points.length);
    this.lowerBound = other.lowerBound;
    this.upperBound = other.upperBound;
  }

  public void run() {
    run(1, 1);
  }

  public void run(int everyNth, int outOf) {
    Location view = owner.getView();

    double distSqr = Math.pow(owner.getDistance(), 2);

    asBoundedList().stream().parallel()
        .filter(e -> e.index() % outOf == everyNth)
        .filter(e -> e.internalLocation().distanceSquared(view) < distSqr)
        .forEach(e -> owner.playParticle(e.platformLocation()));
  }

  public List<Point<LocationT>> asList() {
    return new LinkedList<>(Arrays.asList(points));
  }

  public List<Point<LocationT>> asBoundedList() {
    return new LinkedList<>(Arrays.asList(points)
        .subList(Math.max(lowerBound, 0), Math.min(upperBound, points.length)));
  }

  public void resetBounds() {
    this.lowerBound = 0;
    this.upperBound = points.length;
  }

  record Point<LocationT>(Location internalLocation, LocationT platformLocation, int index) {
  }
}
