package de.cubbossa.pathfinder.module.visualizing.visualizer;

import com.google.auto.service.AutoService;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderExtension;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.module.visualizing.AbstractVisualizerType;
import org.jetbrains.annotations.NotNull;

@AutoService(PathFinderExtension.class)
public class ScriptedVisualizerPathfinderExtension implements PathFinderExtension {

  public static final NamespacedKey KEY = PathPlugin.pathfinder("scriptline-visualizers");
  public static AbstractVisualizerType<ScriptLineParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE =
      new ScriptLineParticleVisualizerType(PathPlugin.pathfinder("scriptline"));

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    pathPlugin.getVisualizerTypeRegistry().registerVisualizerType(ADV_PARTICLE_VISUALIZER_TYPE);
  }
}
