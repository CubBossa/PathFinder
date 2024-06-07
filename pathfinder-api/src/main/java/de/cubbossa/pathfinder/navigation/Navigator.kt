package de.cubbossa.pathfinder.navigation;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.PathView;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.List;

public interface Navigator extends Disposable {

  List<Node> createPath(Route route) throws NoPathFoundException;

  <PlayerT> VisualizerPath<PlayerT> createRenderer(
      PathPlayer<PlayerT> viewer, Route route
  ) throws NoPathFoundException;

  <PlayerT, ViewT extends PathView<PlayerT>> VisualizerPath<PlayerT> createRenderer(
      PathPlayer<PlayerT> viewer, Route route, PathVisualizer<ViewT, PlayerT> renderer
  ) throws NoPathFoundException;
}
