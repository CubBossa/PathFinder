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
  final List<Node> path;
  Timer timer;

  public AbstractVisualizerPath(List<Node> path) {
    this.path = path;
  }

  public void setTargetViewer(PathPlayer<PlayerT> targetViewer) {
    this.targetViewer = targetViewer;
    this.addViewer(targetViewer);
  }

  @Override
  public List<Node> getPath() {
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
    if (timer != null) {
      timer.cancel();
    }
    timer = new Timer();
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
