package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.misc.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractParticlePlayer<LocationT> extends TimerTask {

  private record Point<LocationT>(Location internalLocation, LocationT platformLocation, int index) {
  }

  interface PathSupplier<LocationT> {

    /**
     * @param view     The view that is receiving particles.
     * @param distance The maximal allowed distance to the view
     * @return A location-index map, because the step attribute is working with particle indices and when removing particles,
     * the index must somehow be preserved.
     */
    List<Point<LocationT>> getPath(Location view, double distance);

  }

  /**
   * Interval in milliseconds
   */
  @Getter
  @Setter
  private int interval;
  /**
   * Amount of points to leave out before rendering the next point
   */
  @Getter
  @Setter
  private int steps;
  /**
   * rendering distance for player
   */
  @Getter
  @Setter
  private int distance;

  private final PathSupplier<LocationT> pathSupplier;
  private Timer timer;
  private final AtomicInteger currentStep = new AtomicInteger(0);
  private final List<Point<LocationT>> path;

  private Location lastView = null;
  private List<Point<LocationT>> lastPath = new ArrayList<>();

  public AbstractParticlePlayer(List<Location> path) {
    this.path = new ArrayList<>();
    int i = 0;
    for (Location loc : path) {
      this.path.add(new Point<>(loc, convert(loc), i++));
    }

    this.pathSupplier = (view, d) -> {
      double squaredDistance = Math.pow(d, 2);
      return this.path.stream().parallel()
          .filter(e -> e.internalLocation().distanceSquared(view) < squaredDistance)
          .toList();
    };
  }

  abstract Location getView();

  abstract void playParticle(LocationT location);

  abstract LocationT convert(Location location);

  public void start() {
    if (timer == null) {
      timer = new Timer();
    } else {
      timer.cancel();
    }
    timer.schedule(this, 0, interval);
  }

  public void stop() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
  }

  @Override
  public void run() {
    Location view = getView();
    int s = currentStep.getAndIncrement();
    if (s + 1 >= steps) {
      currentStep.set(0);
    }
    List<Point<LocationT>> path = lastView.toBlockCoordinates().equals(view.toBlockCoordinates())
        ? lastPath
        : pathSupplier.getPath(view, distance);
    lastView = view;
    lastPath = path;

    path.stream().parallel()
        .filter(e -> e.index() % steps == s)
        .forEach(e -> playParticle(e.platformLocation()));
  }
}
