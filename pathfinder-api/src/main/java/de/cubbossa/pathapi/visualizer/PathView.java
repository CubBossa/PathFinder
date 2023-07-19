package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.PathPlayer;

import java.util.Collection;

public interface PathView<PlayerT> {

  PathPlayer<PlayerT> getTargetViewer();

  void setTargetViewer(PathPlayer<PlayerT> player);

  void addViewer(PathPlayer<PlayerT> player);

  void removeViewer(PathPlayer<PlayerT> player);

  void removeAllViewers();

  Collection<PathPlayer<PlayerT>> getViewers();
}
