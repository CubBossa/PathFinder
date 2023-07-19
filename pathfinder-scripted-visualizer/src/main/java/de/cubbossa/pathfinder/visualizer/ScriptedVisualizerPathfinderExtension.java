package de.cubbossa.pathfinder.visualizer;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.visualizer.impl.InternalVisualizerStorage;
import org.jetbrains.annotations.NotNull;

@AutoService(PathFinderExtension.class)
public class ScriptedVisualizerPathfinderExtension implements PathFinderExtension {

    public static final NamespacedKey KEY = CommonPathFinder.pathfinder("scriptline-visualizers");
    public static AbstractVisualizerType<ScriptLineParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE =
            new ScriptLineParticleVisualizerType(CommonPathFinder.pathfinder("scriptline"));

  @NotNull
  @Override
  public NamespacedKey getKey() {
    return KEY;
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    if (pathPlugin.getStorage().getImplementation() instanceof InternalVisualizerDataStorage storage) {
      ADV_PARTICLE_VISUALIZER_TYPE.setStorage(new InternalVisualizerStorage<>(ADV_PARTICLE_VISUALIZER_TYPE, storage));
    }
    pathPlugin.getVisualizerTypeRegistry().registerVisualizerType(ADV_PARTICLE_VISUALIZER_TYPE);
  }
}
