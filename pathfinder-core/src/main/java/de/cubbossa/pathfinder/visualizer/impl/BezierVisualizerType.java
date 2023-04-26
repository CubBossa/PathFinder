package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.commands.CustomArgs;
import de.cubbossa.pathfinder.commands.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;

public abstract class BezierVisualizerType<VisualizerT extends BezierPathVisualizer>
    extends InternalVisualizerType<VisualizerT>
    implements VisualizerTypeCommandExtension {

  public BezierVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
                                        int argumentOffset) {
    return tree
        .then(CustomArgs.literal("point-distance")
            .then(new FloatArgument("distance", .02f, 100)
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof AdvancedParticleVisualizer vis) { //TODO this should be in its own deriving class
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (Integer) objects[1], "particle-steps",
                            true, vis::getSchedulerSteps, vis::setSchedulerSteps);
                  }
                })))
        .then(CustomArgs.literal("sample-rate")
            .then(CustomArgs.integer("sample-rate", 1, 64)
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof BezierPathVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (Integer) objects[1], "sample-rate", true,
                            vis::getBezierSamplingRate, vis::setBezierSamplingRate);
                  }
                })));
  }
}
