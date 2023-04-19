package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.node.NodeType;
import de.cubbossa.pathfinder.api.visualizer.VisualizerType;

import java.util.function.Consumer;

public interface PathFinderExtension extends Keyed {

  default void onLoad(PathFinder pathPlugin) {}

  default void onEnable(PathFinder pathPlugin) {}

  default void onDisable(PathFinder pathPlugin) {}

  default void registerMessages(Consumer<Class<?>> messageClass) {

  }

  default void registerVisualizerType(Consumer<VisualizerType<?>> typeConsumer) {

  }

  default void registerNodeType(Consumer<NodeType<?>> typeConsumer) {

  }
}
