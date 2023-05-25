package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;

public abstract class BezierVisualizerType<VisualizerT extends BezierPathVisualizer>
    extends InternalVisualizerType<VisualizerT>
    implements VisualizerTypeCommandExtension {

  public BezierVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return tree
        .then(CustomArgs.literal("point-distance")
            .then(new FloatArgument("distance", .02f, 100)
                .executes((commandSender, args) -> {
                  if (args.get(0) instanceof AdvancedParticleVisualizer vis) { //TODO this should be in its own deriving class
                    setProperty(BukkitUtils.wrap(commandSender), vis, args.getUnchecked(1), "particle-steps",
                        true, vis::getSchedulerSteps, vis::setSchedulerSteps);
                  }
                })))
        .then(CustomArgs.literal("sample-rate")
            .then(CustomArgs.integer("sample-rate", 1, 64)
                .executes((commandSender, args) -> {
                  if (args.get(0) instanceof BezierPathVisualizer vis) {
                    setProperty(BukkitUtils.wrap(commandSender), vis, args.getUnchecked(1), "sample-rate", true,
                        vis::getBezierSamplingRate, vis::setBezierSamplingRate);
                  }
                })));
  }
}
