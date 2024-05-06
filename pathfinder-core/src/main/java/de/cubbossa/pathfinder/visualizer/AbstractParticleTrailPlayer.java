package de.cubbossa.pathfinder.visualizer;

import com.google.common.base.Preconditions;
import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.Location;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

public class AbstractParticleTrailPlayer<LocationT> implements Disposable {

  private final AbstractParticlePlayer<LocationT> owner;

  @Getter
  private int lowerBound = 0;
  @Getter
  private int upperBound = 0;
  private final Point<LocationT>[] points;

  public AbstractParticleTrailPlayer(AbstractParticlePlayer<LocationT> owner, List<Location> points) {
    Preconditions.checkArgument(!points.isEmpty());
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

  public void setLowerBound(int lowerBound) {
    this.lowerBound = Math.max(0, lowerBound);
  }

  public void setUpperBound(int upperBound) {
    this.upperBound = Math.min(points.length, upperBound);
  }

  public List<Point<LocationT>> asList() {
    return new LinkedList<>(Arrays.asList(points));
  }

  public List<Point<LocationT>> asBoundedList() {
    return new LinkedList<>(Arrays.asList(points).subList(lowerBound, upperBound));
  }

  public void resetBounds() {
    this.lowerBound = 0;
    this.upperBound = points.length;
  }

  record Point<LocationT>(Location internalLocation, LocationT platformLocation, int index) {
  }
}
