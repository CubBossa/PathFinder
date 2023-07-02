package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.InternalVisualizerType;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import org.bukkit.NamespacedKey;

public abstract class BezierVisualizerType<T extends BezierPathVisualizer<T>>
    extends InternalVisualizerType<T> {

  public BezierVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,
                                       int argumentOffset) {
    return tree
        .then(CustomArgs.literal("point-distance")
            .then(new FloatArgument("distance", .02f, 100)
                .executes((commandSender, objects) -> {
                  if (objects.get(0) instanceof AdvancedParticleVisualizer<?> vis) { //TODO this should be in its own deriving class
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, objects.<Integer>getUnchecked(1), "particle-steps",
                            true, vis::getSchedulerSteps, vis::setSchedulerSteps);
                  }
                })))
        .then(CustomArgs.literal("sample-rate")
            .then(CustomArgs.integer("sample-rate", 1, 64)
                .executes((commandSender, objects) -> {
                  if (objects.get(0) instanceof BezierPathVisualizer<?> vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, objects.<Integer>getUnchecked(1), "sample-rate", true,
                            vis::getBezierSamplingRate, vis::setBezierSamplingRate);
                  }
                })));
  }
}
