package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.api.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Keyed;

public interface PathPluginExtension extends Keyed {

  default void onLoad(PathPlugin pathPlugin) {}

  default void onEnable(PathPlugin pathPlugin) {}

  default void onDisable(PathPlugin pathPlugin) {}

  default void registerMessages(Consumer<Class<?>> messageClass) {

  }

  default void registerVisualizerType(Consumer<VisualizerType<?>> typeConsumer) {

  }

  default void registerNodeType(Consumer<NodeType<?>> typeConsumer) {

  }

  default Map<Integer, Node> loadNodes() {
    return new HashMap<>();
  }

  default HashedRegistry<PathVisualizer<?, ?>> loadVisualizer() {
    return new HashedRegistry<>();
  }
}
