package de.cubbossa.pathfinder.api.visualizer;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.Named;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.misc.PermissionHolder;
import de.cubbossa.pathfinder.api.node.Node;

import java.util.List;

public interface PathVisualizer<T extends PathVisualizer<T, D, P>, D, P> extends Keyed, Named, PermissionHolder {

  VisualizerType<T> getType();

  Class<P> getTargetType();

  D prepare(List<Node<?>> nodes, PathPlayer<P> player);

  void play(VisualizerContext<D, P> context);

  void destruct(PathPlayer<P> player, D data);

  int getInterval();

  void setInterval(int interval);

  //TODO interface
  record VisualizerContext<D, P>(List<PathPlayer<P>> players, int interval, long time, D data) {

    public PathPlayer<P> player() {
      return players.get(0);
    }
  }
}
