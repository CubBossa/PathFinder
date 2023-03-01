package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

@Getter
public class PathPluginExtension implements Keyed {

  private final NamespacedKey key;

  public PathPluginExtension(NamespacedKey key) {
    this.key = key;
  }

  public void onEnable() {

  }

  public void onDisable() {

  }

  public void registerMessages(Consumer<Class<?>> messageClass) {

  }

  public void registerVisualizerType(Consumer<VisualizerType<?>> typeConsumer) {

  }

  public void registerNodeType(Consumer<NodeType<?>> typeConsumer) {

  }

  public Map<Integer, Node> loadNodes() {
    return new HashMap<>();
  }

  public HashedRegistry<RoadMap> loadRoadMaps() {
    return new HashedRegistry<>();
  }

  public HashedRegistry<PathVisualizer<?, ?>> loadVisualizer() {
    return new HashedRegistry<>();
  }
}
