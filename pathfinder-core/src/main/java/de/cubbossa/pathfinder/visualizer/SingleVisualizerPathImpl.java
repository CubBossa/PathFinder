package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;

public class SingleVisualizerPathImpl<PlayerT, ViewT extends PathView<PlayerT>> extends AbstractVisualizerPath<PlayerT> {

  private final ViewT view;

  public SingleVisualizerPathImpl(UpdatingPath route, PathVisualizer<ViewT, PlayerT> visualizer, PathPlayer<PlayerT> targetViewer) {
    super(route);
    setTargetViewer(targetViewer);
    this.view = visualizer.createView(route, targetViewer);
    update();
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
    super.update();
    this.view.update();
  }
}
