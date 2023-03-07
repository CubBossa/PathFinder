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

public interface PathPluginExtension extends Keyed {

  default void onLoad() {}

  default void onEnable() {}

  default void onDisable() {}

  default void registerMessages(Consumer<Class<?>> messageClass) {

  }

  default void registerVisualizerType(Consumer<VisualizerType<?>> typeConsumer) {

  }

  default void registerNodeType(Consumer<NodeType<?>> typeConsumer) {

  }

  default Map<Integer, Node> loadNodes() {
    return new HashMap<>();
  }

  default HashedRegistry<RoadMap> loadRoadMaps() {
    return new HashedRegistry<>();
  }

  default HashedRegistry<PathVisualizer<?, ?>> loadVisualizer() {
    return new HashedRegistry<>();
  }
}