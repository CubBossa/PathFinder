package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.Keyed;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.PathView;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedVisualizer extends
    AbstractVisualizer<CombinedVisualizer.CombinedView<Object>, Object> {

  private final List<NamespacedKey> visualizerKeys;
  private final List<PathVisualizer<?, ?>> visualizers;
  private boolean referencesResolved = false;

  public CombinedVisualizer(NamespacedKey key) {
    super(key);
    visualizerKeys = new ArrayList<>();
    visualizers = new ArrayList<>();
  }

  @Override
  public Class<Object> getTargetType() {
    return Object.class;
  }

  public void addVisualizer(NamespacedKey visualizer) {
    this.visualizerKeys.add(visualizer);
    referencesResolved = false;
  }

  public void addVisualizer(PathVisualizer<?, ?> visualizer) {
    this.visualizerKeys.add(visualizer.getKey());
    this.visualizers.add(visualizer);
  }

  public void removeVisualizer(PathVisualizer<?, ?> visualizer) {
    this.visualizerKeys.remove(visualizer.getKey());
    this.visualizers.remove(visualizer);
  }

  public void clearVisualizers() {
    this.visualizerKeys.clear();
    this.visualizers.clear();
  }

  public List<PathVisualizer<?, ?>> getVisualizers() {
    return new ArrayList<>(visualizers);
  }

  public void resolveReferences(Collection<PathVisualizer<?, ?>> scope) {
    if (referencesResolved) {
      return;
    }
    visualizers.clear();
    visualizers.addAll(
        scope.stream().filter(visualizer -> visualizerKeys.contains(visualizer.getKey())).toList());
    if (visualizers.size() == visualizerKeys.size()) {
      referencesResolved = true;
    }
  }

  @Override
  public CombinedView<Object> createView(List<Node> nodes, PathPlayer<Object> player) {
    return new CombinedView<Object>(player, visualizers.stream()
        .filter(v -> v.getTargetType().isInstance(getTargetType()))
        // safely assume that the player type matches for all sub visualizers that matched the filter
        .collect(Collectors.toMap(Keyed::getKey, v -> v.createView(nodes, (PathPlayer) player))));
  }

  @Getter
  public class CombinedView<T> extends AbstractVisualizer<CombinedView<T>, T>.AbstractView {

    private final Map<NamespacedKey, PathView<T>> childData;

    public CombinedView(PathPlayer<T> targetViewer, Map<NamespacedKey, PathView<T>> childData) {
      super(targetViewer);
      this.childData = childData;
    }

    @Override
    public void addViewer(PathPlayer<T> player) {
      childData.values().forEach(pathView -> pathView.addViewer(player));
    }

    @Override
    public void removeViewer(PathPlayer<T> player) {
      childData.values().forEach(pathView -> pathView.removeViewer(player));
    }

    @Override
    public void removeAllViewers() {
      childData.values().forEach(PathView::removeAllViewers);
    }

    @Override
    public Collection<PathPlayer<T>> getViewers() {
      return childData.values().stream().map(PathView::getViewers).flatMap(Collection::stream).collect(Collectors.toSet());
    }
  }
}
