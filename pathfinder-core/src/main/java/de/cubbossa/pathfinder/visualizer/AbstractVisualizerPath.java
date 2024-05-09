package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;

public abstract class AbstractVisualizerPath<PlayerT> implements VisualizerPath<PlayerT> {

  @Getter
  PathPlayer<PlayerT> targetViewer = null;
  final Collection<PathPlayer<PlayerT>> viewers = new HashSet<>();
  final UpdatingPath path;
  final List<Node> pathCache;
  Timer timer;

  public AbstractVisualizerPath(UpdatingPath path) {
    this.path = path;
    this.pathCache = new ArrayList<>();
  }

  @Override
  public void dispose() {
    VisualizerPath.super.dispose();
    removeAllViewers();
    stopUpdater();
  }

  @Override
  public void update() {
    pathCache.clear();
    try {
      pathCache.addAll(path.getNodes());
    } catch (NoPathFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void setTargetViewer(PathPlayer<PlayerT> targetViewer) {
    this.targetViewer = targetViewer;
    this.addViewer(targetViewer);
  }

  public List<Node> getPath() {
    return pathCache;
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
    if (interval <= 0) {
      return;
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
