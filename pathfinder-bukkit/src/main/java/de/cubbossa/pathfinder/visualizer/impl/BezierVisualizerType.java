package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;

public abstract class BezierVisualizerType<VisualizerT extends BezierPathVisualizer>
    extends IntervalVisualizerType<VisualizerT>
    implements VisualizerTypeCommandExtension {

  public BezierVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    tree = super.appendEditCommand(tree, visualizerIndex, argumentOffset);
    return tree
        .then(subCommand("point-distance", new DoubleArgument("distance", 0.02, 100), BezierPathVisualizer.PROP_POINT_DIST))
        .then(subCommand("sample-rate", new IntegerArgument("distance", 1, 64), BezierPathVisualizer.PROP_SAMPLE_RATE));
  }
}
