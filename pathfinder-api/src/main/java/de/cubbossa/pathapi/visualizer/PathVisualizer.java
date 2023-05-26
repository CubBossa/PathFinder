package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.PermissionHolder;
import de.cubbossa.pathapi.node.Node;

import java.util.List;

public interface PathVisualizer<DataT, PlayerT> extends Keyed, PermissionHolder {

  Class<PlayerT> getTargetType();

  DataT prepare(List<Node> nodes, PathPlayer<PlayerT> player);

  void play(VisualizerContext<DataT, PlayerT> context);

  void destruct(PathPlayer<PlayerT> player, DataT data);

  int getInterval();

  void setInterval(int interval);

  record VisualizerContext<DataT, PlayerT>(List<PathPlayer<PlayerT>> players, int interval, long time, DataT data) {

    public PathPlayer<PlayerT> player() {
      return players.get(0);
    }
  }
}
