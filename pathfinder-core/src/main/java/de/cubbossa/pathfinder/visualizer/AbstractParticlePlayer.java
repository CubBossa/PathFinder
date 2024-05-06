package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.misc.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 * The particle player instance handles refreshing paths in a smooth way. At the same time, it reduces bandwidth by
 * only sending packets that are in range of the player.
 * The particle player remembers old paths and continues the illusion of wandering particles by playing initiated steps
 * on the older paths. It does not add "wandering particles" to the old path and only continues already existing ones.
 * New "wandering particles" will always be rendered on the newest path.
 * If a "wandering particle" leaves the view distance of the player, it gets removed and won't be visible, even if the
 * player becomes in range again later.
 *
 * @param <LocationT> An abstraction of the actual location class implementation. For Bukkit org.bukkit.Location.
 */
public abstract class AbstractParticlePlayer<LocationT> extends TimerTask implements Disposable {

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
  private int distance = 16 * 8;
  /**
   * The amount of boundary steps to increment for each step. If set to 1 (default), one "wandering particle"
   * will be continued to the end, while a new one wanders on the updated path.
   */
  @Getter
  @Setter
  private int updateIncrement = 1;

  private Timer timer;
  private final AtomicInteger currentStep = new AtomicInteger(0);

  private AbstractParticleTrailPlayer<LocationT> newestPath;
  private List<AbstractParticleTrailPlayer<LocationT>> oldPaths = new ArrayList<>();

  private volatile List<Location> pathUpdate = null;

  @Override
  public void dispose() {
    Disposable.super.dispose();
    oldPaths.add(newestPath);
    newestPath = null;
  }

  /**
   * Checks, if one period elapsed since the last time that the path had been changed.
   * Only if this is the case, the current path gets refreshed. Refreshed means, that all
   * "wandering particles" produced by the steps continue their original path, but new
   * "wandering particles" follow the newly set path.
   *
   * @param path The path list.
   */
  public void setNewestPath(List<Location> path) {
    this.pathUpdate = path;
  }

  public void setNewestPathAndConvert(List<LocationT> path) {
    setNewestPath(path.stream()
        .map(this::convert)
        .collect(Collectors.toList()));
  }

  /**
   * @return The location of the viewer that limits particles by view distance.
   */
  abstract Location getView();

  /**
   * Plays a particle at a location
   *
   * @param location the location to play the particle at.
   */
  abstract void playParticle(LocationT location);

  abstract LocationT convert(Location location);

  abstract Location convert(LocationT location);

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
    int s = currentStep.getAndIncrement();


    // Make sure to apply new path
    if (pathUpdate != null) {
      // Add the previously newest path to the old paths
      if (newestPath != null) {
        oldPaths.add(newestPath);
      }
      // Make path update the newest path
      newestPath = new AbstractParticleTrailPlayer<>(this, pathUpdate);
      PathFinderProvider.get().getDisposer().register(this, newestPath);
      newestPath.setLowerBound(0);
      newestPath.setUpperBound(updateIncrement);
      pathUpdate = null;
    }
    if (s + 1 >= steps) {
      currentStep.set(0);
    }

    if (newestPath != null) {
      newestPath.setUpperBound(newestPath.getUpperBound() + updateIncrement);
      newestPath.run(s, steps);
    }

    oldPaths = oldPaths.stream().parallel()
        .filter(Objects::nonNull)
        .peek(p -> p.setUpperBound(p.getUpperBound() + updateIncrement))
        .peek(p -> p.setLowerBound(p.getLowerBound() + updateIncrement))
        .filter(p -> p.getUpperBound() > p.getLowerBound())
        .peek(p -> p.run(s, steps))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
