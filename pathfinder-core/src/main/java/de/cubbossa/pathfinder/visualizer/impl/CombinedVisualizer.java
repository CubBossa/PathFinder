package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;

public class CombinedVisualizer extends
    AbstractVisualizer<CombinedVisualizer, CombinedVisualizer.CombinedData, Object> {

  private final List<NamespacedKey> visualizerKeys;
  private final List<PathVisualizer<?, ?, ?>> visualizers;
  private boolean referencesResolved = false;

  public CombinedVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
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

  public void addVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    this.visualizerKeys.add(visualizer.getKey());
    this.visualizers.add(visualizer);
  }

  public void removeVisualizer(PathVisualizer<?, ?, ?> visualizer) {
    this.visualizerKeys.remove(visualizer.getKey());
    this.visualizers.remove(visualizer);
  }

  public void clearVisualizers() {
    this.visualizerKeys.clear();
    this.visualizers.clear();
  }

  public List<PathVisualizer<?, ?, ?>> getVisualizers() {
    return new ArrayList<>(visualizers);
  }

  @Override
  public VisualizerType<CombinedVisualizer> getType() {
    return VisualizerHandler.COMBINED_VISUALIZER_TYPE;
  }

  public void resolveReferences(Collection<PathVisualizer<?, ?, ?>> scope) {
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
  public CombinedData prepare(List<Node<?>> nodes, PathPlayer<Object> player) {
    return new CombinedData(visualizers.stream()
        .filter(v -> v.getTargetType().equals(player.getPlayerClass()))
        // safely assume that the player type matches for all sub visualizers that matched the filter
        .collect(Collectors.toMap(Keyed::getKey, v -> v.prepare(nodes, (PathPlayer) player))));
  }

  @Override
  public void play(VisualizerContext<CombinedData, Object> context) {
    visualizers.forEach(visualizer -> visualizer.play(
        new VisualizerContext(context.players(), context.interval(), context.time(),
            context.data().childData().get(visualizer.getKey()))));
  }

  @Override
  public void destruct(PathPlayer<Object> player, CombinedData data) {
  }

  public record CombinedData(Map<NamespacedKey, Object> childData) {
  }
}
