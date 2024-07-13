package de.cubbossa.pathfinder.navigation;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.List;

public interface Navigation<PlayerT> extends Disposable {

  PathPlayer<PlayerT> viewer();

  VisualizerPath<PlayerT> renderer();

  NavigationLocation startLocation();

  NavigationLocation endLocation();

  List<Location> pathControlPoints();

  void complete();

  void cancel();

  Navigation<PlayerT> persist();

  Navigation<PlayerT> cancelWhenTargetInRange();

  Navigation<PlayerT> cancelWhenTargetInRange(double range);

  void onEnd(Runnable runnable);

  void onComplete(Runnable runnable);

  void onCancel(Runnable runnable);
}
