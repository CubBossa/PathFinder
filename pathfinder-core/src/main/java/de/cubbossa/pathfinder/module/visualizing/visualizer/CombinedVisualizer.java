package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.cubbossa.pathfinder.api.visualizer.VisualizerType;
import org.bukkit.Keyed;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.entity.Player;

public class CombinedVisualizer
		implements PathVisualizer<CombinedVisualizer, CombinedVisualizer.CombinedData> {

  private final List<NamespacedKey> visualizerKeys;
  private final List<PathVisualizer<?, ?>> visualizers;
  private boolean referencesResolved = false;

  public CombinedVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
    visualizerKeys = new ArrayList<>();
    visualizers = new ArrayList<>();
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

  @Override
  public VisualizerType<CombinedVisualizer> getType() {
    return VisualizerHandler.COMBINED_VISUALIZER_TYPE;
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
  public CombinedData prepare(List<Node<?>> nodes, Player player) {
    return new CombinedData(visualizers.stream()
        .collect(Collectors.toMap(Keyed::getKey, v -> v.prepare(nodes, player))));
  }

  @Override
  public void play(VisualizerContext<CombinedData> context) {
    visualizers.forEach(visualizer -> visualizer.play(
        new VisualizerContext(context.players(), context.interval(), context.time(),
            context.data().childData().get(visualizer.getKey()))));
  }

  public record CombinedData(Map<NamespacedKey, Object> childData) {
  }
}
