package de.cubbossa.pathapi;

import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.node.NodeType;

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
