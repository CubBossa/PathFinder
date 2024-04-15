package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import java.util.List;

public class SingleVisualizerPathImpl<PlayerT, ViewT extends PathView<PlayerT>> extends AbstractVisualizerPath<PlayerT> {

  private final ViewT view;

  public SingleVisualizerPathImpl(List<Node> route, PathVisualizer<ViewT, PlayerT> visualizer, PathPlayer<PlayerT> targetViewer) {
    super(route);
    setTargetViewer(targetViewer);
    this.view = visualizer.createView(route, targetViewer);
  }

  @Override
  public void addViewer(PathPlayer<PlayerT> player) {
    super.addViewer(player);
    this.view.addViewer(player);
  }

  @Override
  public void removeViewer(PathPlayer<PlayerT> player) {
    super.removeViewer(player);
    this.view.removeViewer(player);
  }

  @Override
  public void removeAllViewers() {
    super.removeAllViewers();
    this.view.removeAllViewers();
  }

  @Override
  public void setTargetViewer(PathPlayer<PlayerT> targetViewer) {
    super.setTargetViewer(targetViewer);
    view.setTargetViewer(targetViewer);
  }

  @Override
  public void update() {
    this.view.update();
  }
}
