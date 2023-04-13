package de.cubbossa.pathfinder.api.visualizer;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.Named;
import de.cubbossa.pathfinder.api.misc.PermissionHolder;
import de.cubbossa.pathfinder.api.node.Node;

import java.util.List;
import java.util.UUID;

public interface PathVisualizer<T extends PathVisualizer<T, D>, D> extends Keyed, Named, PermissionHolder {

  VisualizerType<T> getType();

  D prepare(List<Node<?>> nodes, UUID player);

  void play(VisualizerContext<D> context);

  void destruct(UUID player, D data);

  int getInterval();

  void setInterval(int interval);

  //TODO interface
  record VisualizerContext<D>(List<UUID> players, int interval, long time, D data) {

    public UUID player() {
      return players.get(0);
    }

  }
}
