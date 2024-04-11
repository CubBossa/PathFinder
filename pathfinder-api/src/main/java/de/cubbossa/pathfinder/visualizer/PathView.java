package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.PathPlayer;
import java.util.Collection;

public interface PathView<PlayerT> extends Disposable {

  PathPlayer<PlayerT> getTargetViewer();

  void setTargetViewer(PathPlayer<PlayerT> player);

  void addViewer(PathPlayer<PlayerT> player);

  void removeViewer(PathPlayer<PlayerT> player);

  void removeAllViewers();

  Collection<PathPlayer<PlayerT>> getViewers();

  default void update() {
  }
}
