package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import org.bukkit.NamespacedKey;

public abstract class BezierVisualizerType<T extends BezierPathVisualizer<T>>
    extends VisualizerType<T> {

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
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (Integer) objects[1], "particle-steps",
                            true, vis::getSchedulerSteps, vis::setSchedulerSteps);
                  }
                })))
        .then(CustomArgs.literal("sample-rate")
            .then(CustomArgs.integer("sample-rate", 1, 64)
                .executes((commandSender, objects) -> {
                  if (objects[0] instanceof ScriptLineParticleVisualizer vis) {
                    VisualizerHandler.getInstance()
                        .setProperty(commandSender, vis, (Integer) objects[1], "sample-rate", true,
                            vis::getBezierSamplingRate, vis::setBezierSamplingRate);
                  }
                })));
  }
}
