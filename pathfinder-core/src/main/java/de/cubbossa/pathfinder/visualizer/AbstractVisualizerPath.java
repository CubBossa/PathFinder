package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractVisualizerPath<PlayerT> implements VisualizerPath<PlayerT> {

  @Getter
  @Setter
  PathPlayer<PlayerT> targetViewer = null;
  final Collection<PathPlayer<PlayerT>> viewers = new HashSet<>();
  final List<? extends Node> path;
  final Timer timer;

  public AbstractVisualizerPath(List<? extends Node> path) {
    this.path = path;
    this.timer = new Timer();
  }

  @Override
  public List<? extends Node> getPath() {
    return path;
  }

  @Override
  public void addViewer(PathPlayer<PlayerT> player) {
    viewers.add(player);
  }

  @Override
  public void removeViewer(PathPlayer<PlayerT> player) {
    viewers.remove(player);
  }

  @Override
  public void removeAllViewers() {
    viewers.clear();
  }

  @Override
  public Collection<PathPlayer<PlayerT>> getViewers() {
    return viewers;
  }

  @Override
  public boolean isActive() {
    return !viewers.isEmpty();
  }

  @Override
  public boolean isActive(PathPlayer<PlayerT> player) {
    return viewers.contains(player);
  }

  @Override
  public void startUpdater(int interval) {
    timer.cancel();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        update();
      }
    }, 0, interval);
  }

  @Override
  public void stopUpdater() {
    timer.cancel();
  }
}
